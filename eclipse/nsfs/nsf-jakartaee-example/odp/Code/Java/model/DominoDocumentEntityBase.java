/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package model;

import java.util.ArrayList;
import java.util.List;

import jakarta.json.bind.annotation.JsonbTransient;

/**
 * Used to test issue #513
 * 
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/513">https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/513</a>
 */
public abstract class DominoDocumentEntityBase {
    @JsonbTransient
    public List<String> getLog() {
        return this._makeSureIsList(this._getLog());
    }
	
    public void setLog(List<String> log) {
        if (log != null) {
            this._setLog(log);
        }
    }

    protected abstract List<String> _getLog();
    protected abstract void _setLog(List<String> entries);
	
    private <T> List<T> _makeSureIsList(List<T> list) {
        return (list != null) ? list : new ArrayList<>();
    }
}
