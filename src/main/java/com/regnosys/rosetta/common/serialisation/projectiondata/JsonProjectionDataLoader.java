package com.regnosys.rosetta.common.serialisation.projectiondata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.common.serialisation.AbstractJsonDataLoader;
import com.regnosys.rosetta.common.serialisation.reportdata.ReportDataItem;
import com.regnosys.rosetta.common.serialisation.reportdata.ReportDataSet;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JsonProjectionDataLoader extends AbstractJsonDataLoader<ReportDataSet> {

    public static final String DEFAULT_DESCRIPTOR_NAME = "-data-descriptor.json";

    private final URL inputPath;

    public JsonProjectionDataLoader(ClassLoader classLoader,
                                    ObjectMapper rosettaObjectMapper,
                                    URL descriptorPath,
                                    List<String> descriptorFileNames) {
        super(classLoader, rosettaObjectMapper, descriptorPath, descriptorFileNames, ReportDataSet.class, false);
        this.inputPath = null;
    }

    public JsonProjectionDataLoader(ClassLoader classLoader,
                                    ObjectMapper rosettaObjectMapper,
                                    URL descriptorPath,
                                    List<String> descriptorFileNames,
                                    URL inputPath) {
        super(classLoader, rosettaObjectMapper, descriptorPath, descriptorFileNames, ReportDataSet.class, true);
        this.inputPath = inputPath;
    }

    @Override
    public ReportDataSet loadInputFiles(ReportDataSet descriptor) {
        List<ReportDataItem> loadedData = new ArrayList<>();
        for (ReportDataItem data : descriptor.getData()) {
            ReportDataItem reportDataItem;
            try {
                reportDataItem = new ReportDataItem(data.getName(), getInput(descriptor.getInputType(), data, inputPath),
                        data.getExpected()); // expected is handled by JsonExpectedResultLoader
            } catch (RuntimeException e) {
                reportDataItem = new ReportDataItem(data.getName(), data.getInput(), data.getExpected(), e);
            }
            loadedData.add(reportDataItem);
        }
        return new ReportDataSet(descriptor.getDataSetName(), descriptor.getInputType(), descriptor.getApplicableReports(), loadedData);
    }

}