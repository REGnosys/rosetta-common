package com.regnosys.rosetta.common.reports;

import com.regnosys.rosetta.common.Paths;
import com.rosetta.model.lib.ModelReportId;

import java.nio.file.Files;
import java.nio.file.Path;

public class ReportPaths extends Paths {

    public static final Path REGULATORY_REPORTING_PATH = java.nio.file.Paths.get("regulatory-reporting");
    public static final String REPORT_EXPECTATIONS_FILE_NAME = "report-expectations.json";


    public static ReportPaths get(Path resourcesPath) {
        return Files.exists(resourcesPath.resolve(REGULATORY_REPORTING_PATH).resolve(INPUT_PATH)) ?
                ReportPaths.getDefault() : ReportPaths.getLegacy();
    }

    public static ReportPaths getDefault() {
        Path rootPath = REGULATORY_REPORTING_PATH;
        return new ReportPaths(rootPath,
                rootPath.resolve(INPUT_PATH),
                rootPath.resolve(OUTPUT_PATH),
                rootPath.resolve(CONFIG_PATH),
                rootPath.resolve(LOOKUP_PATH));
    }

    public static ReportPaths getLegacy() {
        Path dataPath = REGULATORY_REPORTING_PATH.resolve(LEGACY_DATA_PATH);
        Path lookup = REGULATORY_REPORTING_PATH.resolve(LOOKUP_PATH);
        return new ReportPaths(dataPath, dataPath, dataPath, dataPath, lookup);
    }

    public ReportPaths(Path rootPath, Path input, Path output, Path config, Path lookup) {
        super(rootPath, input, output, config, lookup);
    }

    public static Path getReportExpectationsFilePath(Path outputPath, ModelReportId reportIdentifier, String dataSetName) {
        return getOutputDataSetPath(outputPath, reportIdentifier, dataSetName).resolve(REPORT_EXPECTATIONS_FILE_NAME);
    }

    @Deprecated
    public static Path getLegacyReportPath(Path outputPath, ModelReportId reportIdentifier) {
        return outputPath.resolve(legacyDirectoryName(reportIdentifier));
    }

    @Deprecated
    public static Path getLegacyKeyValueExpectationFilePath(Path outputPath, ModelReportId reportIdentifier, String dataSetName, Path inputPath) {
        return getLegacyReportDataSetPath(outputPath, reportIdentifier, dataSetName)
                .resolve(inputPath.getFileName().toString().replace(".json", KEY_VALUE_FILE_NAME_SUFFIX));
    }

    public static Path getReportExpectationFilePath(Path outputPath, ModelReportId reportIdentifier, String dataSetName, Path inputPath) {
        return getOutputDataSetPath(outputPath, reportIdentifier, dataSetName)
                .resolve(inputPath.getFileName().toString().replace(".json", REPORT_FILE_NAME_SUFFIX));
    }


    @Deprecated
    public static String legacyDirectoryName(ModelReportId id) {
        return id.joinRegulatoryReference("", "-")
                .replace("_", "-")
                .toLowerCase();
    }
    @Deprecated
    public static Path getLegacyReportDataSetPath(Path outputPath, ModelReportId reportIdentifier, String dataSetName) {
        return getLegacyReportPath(outputPath, reportIdentifier).resolve(directoryNameOfDataset(dataSetName));
    }

}
