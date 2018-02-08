package de.codingair.codingapi.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
 * @verions: 1.0.0
 **/

public class ClassSaver<T> {
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	private File file;
	private Class<? extends T> defaults;
	private T config;
	private boolean temp;
	
	public ClassSaver(File file, Class<? extends T> defaults, boolean temp) {
		this.file = file;
		this.defaults = defaults;
		this.temp = temp;
		
		this.load(temp);
	}
	
	private void load(boolean temp) {
		if((this.file.getParentFile() != null) && (!this.file.getParentFile().exists())) {
			this.file.getParentFile().mkdir();
		}
		
		if((temp) && (this.file.exists())) {
			this.file.delete();
		}
		
		boolean createdNewFile = false;
		
		if(!this.file.exists()) {
			try {
				this.file.createNewFile();
				createdNewFile = true;
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		
		try {
			this.config = gson.fromJson(createdNewFile ? gson.toJson(this.defaults.newInstance()) : IOUtils.toString(new FileInputStream(this.file)), this.defaults);
			
			if((!temp) && (this.config == null)) {
				load(true);
			} else if(this.config != null) {
				save();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			
			if(!temp) {
				load(true);
			}
		}
	}
	
	public void save() {
		try {
			PrintWriter w = new PrintWriter(new FileOutputStream(this.file));
			
			w.print(gson.toJson(this.defaults));
			
			w.flush();
			w.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public T getConfig() {
		return config;
	}
}
