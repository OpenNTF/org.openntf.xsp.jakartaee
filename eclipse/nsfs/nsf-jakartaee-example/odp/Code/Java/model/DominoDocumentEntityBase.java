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
