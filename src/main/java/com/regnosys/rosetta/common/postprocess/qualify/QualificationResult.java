package com.regnosys.rosetta.common.postprocess.qualify;

import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.qualify.QualifyResult;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QualificationResult {

    private final Class<?> qualifiedRosettaObjectType;
    private final List<QualifyResult> allQualifyResults;

    public QualificationResult(Class<?> qualifiedRosettaObjectType,
                               List<QualifyResult> allQualifyResults) {
        this.qualifiedRosettaObjectType = qualifiedRosettaObjectType;
        this.allQualifyResults = allQualifyResults;
    }


    public Class<?> getQualifiedRosettaObjectType() {
        return qualifiedRosettaObjectType;
    }

    /**
     * @return unique successful qualify result if present, otherwise (if unmatched or multiple matches) returns empty.
     */
    public Optional<QualifyResult> getUniqueSuccessQualifyResult() {
        return getSucessQualificationResults().size() == 1 ?
                Optional.of(getSucessQualificationResults().get(0)) : Optional.empty();
    }

    public boolean isSuccess() {
        return getUniqueSuccessQualifyResult().isPresent();
    }

    /**
     * @return qualify results from all executed logic
     */
    public List<QualifyResult> getAllQualifyResults() {
        return allQualifyResults;
    }

    @Override
    public String toString() {
        if (getUniqueSuccessQualifyResult().isPresent()) {
            return String.format("QualificationResult { SUCCESS on [%s:%s] }",
                    qualifiedRosettaObjectType.getSimpleName(),
                    getUniqueSuccessQualifyResult().get().getName());
        } else {
            // Log multiple matches (if there are any)
            List<String> successResults = getSucessQualificationResults()
                    .stream()
                    .map(QualifyResult::getName)
                    .collect(Collectors.toList());
            List<String> errors = allQualifyResults.stream()
                    .filter(r -> !r.isSuccess())
                    .map(this::getQualifyFunctionErrors)
                    .collect(Collectors.toList());
            return String.format("QualificationResult { FAILURE on [%s] because [%s]%s }",
                    qualifiedRosettaObjectType.getSimpleName(),
                    successResults.isEmpty() ?
                            "UNMATCHED" :
                            "MULTIPLE_MATCHES: " + String.join(",", successResults),
                    successResults.isEmpty() ? String.format(", errors: [%s]", errors) : "");
        }
    }

    private List<QualifyResult> getSucessQualificationResults() {
        return allQualifyResults.stream()
                .filter(QualifyResult::isSuccess)
                .collect(Collectors.toList());
    }

    private String getQualifyFunctionErrors(QualifyResult qualifyFunctionResult) {
        List<String> failedFunctionErrors = qualifyFunctionResult.getExpressionDataRuleResults().stream()
                .filter(e -> !e.isSuccess())
                .map(QualifyResult.ExpressionDataRuleResult::getError)
                .collect(Collectors.toList());
        return String.format("%s%s", qualifyFunctionResult.getName(), failedFunctionErrors);
    }
}
