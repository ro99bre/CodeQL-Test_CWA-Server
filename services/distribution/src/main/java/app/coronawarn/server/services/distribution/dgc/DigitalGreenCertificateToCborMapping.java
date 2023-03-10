package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.common.shared.util.SerializationUtils.cborEncode;
import static java.util.function.Predicate.not;

import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.functions.BusinessRuleItemSupplier;
import app.coronawarn.server.services.distribution.dgc.functions.BusinessRuleSupplier;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@Profile("!revocation")
public class DigitalGreenCertificateToCborMapping {

  public static final String DCC_VALIDATION_RULE_JSON_CLASSPATH = "dgc/dcc-validation-rule.json";
  private final DigitalCovidCertificateClient digitalCovidCertificateClient;

  public DigitalGreenCertificateToCborMapping(DigitalCovidCertificateClient digitalCovidCertificateClient) {
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
  }

  /**
   * Construct country rules retrieved from DCC client for CBOR encoding.
   */
  public List<String> constructCountryList() throws FetchBusinessRulesException {
    List<String> countryList = digitalCovidCertificateClient.getCountryList();
    //countryList.removeIf("EU"::equals); //remove might not be supported
    return countryList.stream().filter(not("EU"::equals)).collect(Collectors.toList());
  }

  /**
   * Construct business rules retrieved from DCC client for CBOR encoding. Fetched rules are filtered by rule type
   * parameter which could be 'Acceptance' or 'Invalidation' or 'BoosterNotification'.
   *
   * @param ruleType                 the corresponding rule type that is used for creating the business rules.
   * @param businessRuleItemSupplier a functional interface that provides a function that will provide a list of
   *                                 business rule items.
   * @param businessRuleSupplier     a functional interface that provides a function that will provide single business
   *                                 rules.
   * @return - list of the constructed business rules.
   * @throws DigitalCovidCertificateException - thrown if json validation schema is not found or the validation fails
   *                                          for a specific rule. This exception will propagate and will stop any
   *                                          archive to be published down in the execution.
   */
  public List<BusinessRule> constructRules(RuleType ruleType,
      BusinessRuleItemSupplier<List<BusinessRuleItem>> businessRuleItemSupplier,
      BusinessRuleSupplier<BusinessRule, String, String> businessRuleSupplier)
      throws DigitalCovidCertificateException, FetchBusinessRulesException {
    List<BusinessRuleItem> businessRulesItems = businessRuleItemSupplier.get();

    List<BusinessRule> businessRules = new ArrayList<>();
    for (BusinessRuleItem businessRuleItem : businessRulesItems) {
      BusinessRule businessRule =
          businessRuleSupplier.get(businessRuleItem.getCountry(), businessRuleItem.getHash());
      if (businessRule.getType().equalsIgnoreCase(ruleType.getType())) {
        businessRules.add(businessRule);
      }
    }
    return businessRules;
  }

  /**
   * CBOR encoding of {@code constructCountryList}.
   */
  public byte[] constructCborCountries() throws DigitalCovidCertificateException,
      FetchBusinessRulesException {
    return cborEncodeOrElseThrow(constructCountryList());
  }

  /**
   * CBOR encoding of {@code constructRules}.
   */
  public byte[] constructCborRules(RuleType ruleType,
      BusinessRuleItemSupplier<List<BusinessRuleItem>> businessRuleItemSupplier,
      BusinessRuleSupplier<BusinessRule, String, String> getRuleSupplier)
      throws DigitalCovidCertificateException, FetchBusinessRulesException {
    return cborEncodeOrElseThrow(constructRules(ruleType, businessRuleItemSupplier, getRuleSupplier));
  }

  private byte[] cborEncodeOrElseThrow(Object subject) throws DigitalCovidCertificateException {
    try {
      return cborEncode(subject);
    } catch (JsonProcessingException e) {
      throw new DigitalCovidCertificateException("Cbor encryption failed because of Json processing:", e);
    }
  }

}
