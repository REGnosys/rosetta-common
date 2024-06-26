package com.regnosys.rosetta.common.hashing;

/*-
 * ==============
 * Rune Common
 * ==============
 * Copyright (C) 2018 - 2024 REGnosys
 * ==============
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==============
 */

import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;

/**
 * @author TomForwood
 * a Processor or BuilderProcessor report that contains an integer and allows accumulation too that integer to form a hash
 */
public class IntegerReport implements BuilderProcessor.Report, Processor.Report {
	int result;
	
	public IntegerReport(int result) {
		super();
		this.result = result;
	}

	public void accumulate() {
		result*=31;
	}
	
	public void accumulate(int newValue) {
		result = result*31+newValue;
	}

	public int getResult() {
		return result;
	}

	@Override
	public String toString() {
		return Integer.toHexString(result);
	}
}
