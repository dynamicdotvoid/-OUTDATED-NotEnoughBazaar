package dev.nulloverload.notenoughbazaar.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import dev.nulloverload.notenoughbazaar.NotEnoughBazaar;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class ApiUtils {	
	public static boolean validateKey(String key) {
		boolean succeeded = false;
		try {
			URL url = new URL("https://api.hypixel.net/key?key="+key);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.connect();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			
			while((line = br.readLine())!=null) {
				sb.append(line+"\n");
			}
			
			String jsonResponse = sb.toString();
			JSONObject keyInfo = new JSONObject(jsonResponse);
			
			succeeded = keyInfo.getBoolean("success");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return succeeded;
	}
	public static JSONObject getBazaarProducts() {
		if(NotEnoughBazaar.apiKey=="")Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(MessageUtils.apiKeyNotSet));
		JSONObject bazaarInfo = new JSONObject();
		try {
			URL url = new URL("https://api.hypixel.net/skyblock/bazaar?key="+NotEnoughBazaar.apiKey);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.connect();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			
			while((line = br.readLine())!=null) {
				sb.append(line+"\n");
			}
			
			String jsonResponse = sb.toString();
			bazaarInfo = new JSONObject(jsonResponse);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bazaarInfo;
	}
}
