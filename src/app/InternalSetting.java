package app;

import io.DBsettings;

import java.util.Properties;

public class InternalSetting extends Properties {
	private static final long serialVersionUID = 1L;

	public InternalSetting() {
		super();
		
		put(DBsettings.SchemaVersion.toString(), "2");
	}
}
