package com.regnosys.rosetta.common.ingest;

import com.regnosys.rosetta.common.Paths;

import java.nio.file.Files;
import java.nio.file.Path;


public class IngestPaths extends Paths {

    public static final Path INGEST_PATH = java.nio.file.Paths.get("ingest");
    private static final Path LEGACY_INGEST_INPUT_PATH = java.nio.file.Paths.get("cdm-sample-files");
    private static final Path LEGACY_INGEST_CONFIG_PATH = java.nio.file.Paths.get("ingestions");
    private static final Path LEGACY_INGEST_EXCLUDED_PATH = java.nio.file.Paths.get("config");
    private static final Path LEGACY_INGEST_OUTPUT_PATH = java.nio.file.Paths.get("result-json-files");
    private static final Path LEGACY_INGEST_ANALYTICS_PATH = java.nio.file.Paths.get("mapping-analytics");

    public static final Path EXCLUDED_PATH = java.nio.file.Paths.get("excluded");
    public static final Path ANALYTICS_PATH = java.nio.file.Paths.get("analytics");


    private final Path exculdedPath;
    private final Path analyticsPath;
    public static IngestPaths get(Path resourcesPath) {
        return Files.exists(resourcesPath.resolve(INGEST_PATH).resolve(INPUT_PATH)) ?
                IngestPaths.getDefault() : IngestPaths.getLegacy();
    }

    public static IngestPaths getDefault() {
        Path rootPath = INGEST_PATH;
        return new IngestPaths(rootPath,
                rootPath.resolve(INPUT_PATH),
                rootPath.resolve(OUTPUT_PATH),
                rootPath.resolve(CONFIG_PATH),
                rootPath.resolve(LOOKUP_PATH),
                rootPath.resolve(EXCLUDED_PATH),
                rootPath.resolve(ANALYTICS_PATH));
    }

    public static IngestPaths getLegacy() {

        return new IngestPaths(LEGACY_INGEST_INPUT_PATH, LEGACY_INGEST_INPUT_PATH, LEGACY_INGEST_OUTPUT_PATH, LEGACY_INGEST_CONFIG_PATH, LEGACY_INGEST_INPUT_PATH, LEGACY_INGEST_EXCLUDED_PATH, LEGACY_INGEST_ANALYTICS_PATH);
    }

    public IngestPaths(Path rootPath, Path input, Path output, Path config, Path lookup, Path excluded, Path analyticsPath) {
        super(rootPath, input, output, config, lookup);
        this.exculdedPath = excluded;
        this.analyticsPath = analyticsPath;
    }

    public Path getAnalyticsRelativePath() {
        return analyticsPath;
    }

    public Path getExculdedRelativePath() {
        return exculdedPath;
    }

}
