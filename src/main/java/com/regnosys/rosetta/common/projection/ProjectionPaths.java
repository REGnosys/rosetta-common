package com.regnosys.rosetta.common.projection;

import com.regnosys.rosetta.common.Paths;

import java.nio.file.Path;

public class ProjectionPaths extends Paths {

    public static final Path PROJECTION_PATH = java.nio.file.Paths.get("projection");
    public static final Path ISO20022_PATH = java.nio.file.Paths.get("iso-20022");

    public ProjectionPaths(Path rootPath, Path input, Path output, Path config, Path lookup) {
        super(rootPath, input, output, config, lookup);
    }

    public static ProjectionPaths getProjectionPath() {
        Path projectionPath = PROJECTION_PATH;
        Path isoPath = projectionPath.resolve(ISO20022_PATH);
        return new ProjectionPaths(isoPath,
                isoPath.resolve(INPUT_PATH),
                isoPath.resolve(OUTPUT_PATH),
                isoPath.resolve(CONFIG_PATH),
                isoPath.resolve(LOOKUP_PATH));
    }
}
