package com.regnosys.rosetta.common.serialisation.json.reportdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.common.reports.RegReportIdentifier;
import com.regnosys.rosetta.common.reports.RegReportPaths;
import com.regnosys.rosetta.common.serialisation.InputDataLoader;
import com.regnosys.rosetta.common.util.UrlUtils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.regnosys.rosetta.common.serialisation.JsonDataLoaderUtil.*;

public class JsonExpectedResultLoader implements InputDataLoader<ReportIdentifierDataSet> {

    private final ClassLoader classLoader;
    private final ObjectMapper rosettaObjectMapper;
    private final URL outputPath;

    public JsonExpectedResultLoader(ClassLoader classLoader, ObjectMapper rosettaObjectMapper, URL outputPath) {
        this.classLoader = classLoader;
        this.rosettaObjectMapper = rosettaObjectMapper;
        this.outputPath = outputPath;
    }

    @Override
    public ReportIdentifierDataSet loadInputFiles(ReportIdentifierDataSet descriptor) {
        List<ReportDataItem> loadedData = new ArrayList<>();
        ReportDataSet dataSet = descriptor.getDataSet();
        for (ReportDataItem data : dataSet.getData()) {
            ReportDataItem reportDataItem = new ReportDataItem(data.getName(),
                    data.getInput(),
                    getExpected(descriptor.getReportIdentifier(), dataSet.getDataSetName(), dataSet.getExpectedType(), data));
            loadedData.add(reportDataItem);
        }
        return new ReportIdentifierDataSet(
                descriptor.getReportIdentifier(),
                new ReportDataSet(dataSet.getDataSetName(), dataSet.getInputType(), dataSet.getApplicableReports(), loadedData));
    }

    private Object getExpected(RegReportIdentifier regReportIdentifier, String dataSetName, String expectedType, ReportDataItem data) {
        if (data.getInput() instanceof String) {
            // attempt to load per report expectation file
            Path inputFileName = Paths.get(String.valueOf(data.getInput()));
            Path keyValueExpectationPath = RegReportPaths
                    .getKeyValueExpectationFilePath(UrlUtils.toPath(outputPath), regReportIdentifier, dataSetName, inputFileName);
            if (Files.exists(keyValueExpectationPath)) {
                URL keyValueExpectationUrl = UrlUtils.toUrl(keyValueExpectationPath);
                List<ExpectedResultField> resultFields = readTypeList(ExpectedResultField.class, rosettaObjectMapper, keyValueExpectationUrl);
                ExpectedResult expectedResult =
                        data.getExpected() == null ?
                                new ExpectedResult(new HashMap<>()) :
                                fromObject(data.getExpected(), ExpectedResult.class, rosettaObjectMapper);
                expectedResult.getExpectationsPerReport().put(regReportIdentifier.getName(), resultFields);
                return expectedResult;
            }
        }
        // for backwards compatibility
        if (data.getExpected() == null) {
            return null;
        }
        else {
            // attempt to get the legacy all-reports expectations file
            Class<?> expectedTypeClass = loadClass(expectedType, classLoader);
            if (data.getExpected() instanceof String) {
                // by path
                String expectedFileName = (String) data.getExpected();
                URL expectedUrl = UrlUtils.resolve(outputPath, expectedFileName);
                return readType(expectedTypeClass, rosettaObjectMapper, expectedUrl);
            } else {
                // by object
                return fromObject(data.getExpected(), expectedTypeClass, rosettaObjectMapper);
            }
        }
    }
}