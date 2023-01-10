/**
 * Seed
 * Copyright (C) 2021 EUU⛰ROCKS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.seed.core.codegen.compile;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.seed.InternalException;
import org.seed.core.util.MiscUtils;

public class CompilerException extends InternalException {

	private static final long serialVersionUID = 5748866051489365928L;
	
	private transient final List<Diagnostic<? extends JavaFileObject>> diagnostics;

	CompilerException(String message) {
		super(message);
		diagnostics = null;
	}

	CompilerException(Throwable cause) {
		super(cause);
		diagnostics = null;
	}
	
	CompilerException(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		super(MiscUtils.toString(diagnostics, "\n\n"));
		this.diagnostics = diagnostics;
	}
	
	public CompilerErrors getCompilerErrors() {
		return new CompilerErrors(diagnostics);
	}
	
}
