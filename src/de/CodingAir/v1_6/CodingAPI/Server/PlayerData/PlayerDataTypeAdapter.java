package de.CodingAir.v1_6.CodingAPI.Server.PlayerData;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public class PlayerDataTypeAdapter extends TypeAdapter {
	
	@Override
	public void write(JsonWriter writer, Object o) throws IOException {
		PlayerData playerData = (PlayerData) o;
		
		writer.beginObject();
		writer.name("name").value(playerData.getName());
		writer.name("loadedSpawnChunk").value(playerData.loadedSpawnChunk());
		writer.name("viewDistance").value(playerData.getViewDistance());
		writer.endObject();
	}
	
	@Override
	public Object read(JsonReader reader) throws IOException {
		PlayerData playerData = new PlayerData(this);
		reader.beginObject();
		
		while(reader.hasNext()) {
			switch(reader.nextName()) {
				case "name":
					playerData.setName(reader.nextString());
					break;
				case "loadedSpawnChunk":
					playerData.setLoadedSpawnChunk(reader.nextBoolean());
					break;
				case "viewDistance":
					playerData.setViewDistance(reader.nextInt());
					break;
			}
		}
		
		reader.endObject();
		
		return playerData;
	}
}
