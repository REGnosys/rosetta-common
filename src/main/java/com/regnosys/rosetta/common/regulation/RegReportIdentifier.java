package com.regnosys.rosetta.common.regulation;

import java.util.List;
import java.util.Objects;

public class RegReportIdentifier {

	private final String body;
	private final List<String> corpusList;
	private final String name;
	private final String generatedJavaClassName;

	public RegReportIdentifier(String body, List<String> corpusList, String name, String generatedJavaClassName) {
		this.body = body;
		this.corpusList = corpusList;
		this.name = name;
		this.generatedJavaClassName = generatedJavaClassName;
	}

	public String getBody() {
		return body;
	}

	public List<String> getCorpusList() {
		return corpusList;
	}

	public String getName() {
		return name;
	}

	public String getGeneratedJavaClassName() {
		return generatedJavaClassName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RegReportIdentifier that = (RegReportIdentifier) o;
		return Objects.equals(getBody(), that.getBody()) && Objects.equals(getCorpusList(), that.getCorpusList()) && Objects.equals(getName(), that.getName()) && Objects.equals(getGeneratedJavaClassName(), that.getGeneratedJavaClassName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getBody(), getCorpusList(), getName(), getGeneratedJavaClassName());
	}
}