package besouro.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import besouro.model.action.Action;
import besouro.stream.ActionOutputStream;

public class ActionFileStorage implements ActionOutputStream {

	private FileWriter writer;

	public ActionFileStorage(File file) {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
			writer = new FileWriter(file, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addAction(Action action) {
		try {
			writer.write(String.valueOf(action));
			writer.write("\n");
			writer.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Action[] loadFromFile(File file) {
		if (!file.exists()) {
			return null;
		}
		
		try {
			List<Action> list = new ArrayList<Action>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				list.add(Action.fromString(line));
			}
			
			reader.close();
			return list.toArray(new Action[list.size()]);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
