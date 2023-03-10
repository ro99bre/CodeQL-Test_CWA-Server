package app.coronawarn.server.services.distribution.dcc.decode;

import static app.coronawarn.server.common.shared.util.SecurityUtils.ecdsaSignatureVerification;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getEcdsaEncodeFromSignature;
import static app.coronawarn.server.common.shared.util.SecurityUtils.getPublicKeyFromString;
import static app.coronawarn.server.common.shared.util.SerializationUtils.cborEncode;
import static app.coronawarn.server.common.shared.util.SerializationUtils.jsonExtractCosePayload;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.exception.DscListDecodeException;
import com.upokecenter.cbor.CBORObject;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DccRevocationListDecoder {

  private static final Logger logger = LoggerFactory.getLogger(DccRevocationListDecoder.class);

  private DistributionServiceConfig distributionServiceConfig;

  public DccRevocationListDecoder(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Decode the trust list of certificates. Verifies the trust list content by using the ECDSA signature logic. Filters
   * only X509 valid format certificates from the response.
   *
   * @param data - trust list response from DSC as string.
   * @return - object wrapping the list of certificates.
   * @throws DscListDecodeException - thrown if any exception is caught and special treatment if signature verification
   *                                fails.
   */
  public List<RevocationEntry> decode(byte[] data) throws DccRevocationListDecodeException {
    ArrayList<RevocationEntry> revocationEntries = new ArrayList<>();
    try {

      PublicKey publicKey = getPublicKeyFromString(
          distributionServiceConfig.getDccRevocation().getClient().getPublicKey());
      CBORObject cborObject = CBORObject.DecodeFromBytes(data);
      byte[] ecdsaSignature = getEcdsaEncodeFromSignature(cborObject.get(3).GetByteString());
      ArrayList<Object> signedPayload = new ArrayList<>(Arrays.asList("Signature1", cborObject.get(0).GetByteString(),
          new byte[0], cborObject.get(2).GetByteString()));

      ecdsaSignatureVerification(ecdsaSignature, publicKey, cborEncode(signedPayload));

      Map<byte[], List<byte[]>> jsonPayload = jsonExtractCosePayload(data);

      jsonPayload.forEach((keyAndType, values) -> {
        byte[] kid = Arrays.copyOfRange(keyAndType, 0, keyAndType.length - 1);
        byte[] type = Arrays.copyOfRange(keyAndType, keyAndType.length - 1, keyAndType.length);

        values.forEach(hash ->
            revocationEntries.add(new RevocationEntry(kid, type, hash)));
      });
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new DccRevocationListDecodeException("DCC revocation list NOT decoded.", e);
    }
    return revocationEntries;
  }
}
