package dev.nulloverload.notenoughbazaar.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import dev.nulloverload.notenoughbazaar.NotEnoughBazaar;
import dev.nulloverload.notenoughbazaar.utilities.MessageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class FileHandler {
	private static String expectedDirLocation = System.getenv("APPDATA") + "/.minecraft/notenoughbazaar";
	public static String pathApi = System.getenv("APPDATA") + "/.minecraft/notenoughbazaar/key.txt";
	public static File apiTextFile = new File(pathApi);
	public static FileReader fr;
	public static EntityPlayer player;
	
	@SubscribeEvent
	public void onPlayerJoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		player = Minecraft.getMinecraft().thePlayer;
		File expectedDir = new File(expectedDirLocation);
		
		if(expectedDir.exists()) {
			try {
				if(apiTextFile.exists()) {
					BufferedReader brApi = new BufferedReader(new FileReader(apiTextFile));
					NotEnoughBazaar.apiKey = brApi.readLine();
					brApi.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("OOPS __________________________________________\n\n\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			expectedDir.mkdir();
			player.addChatMessage(new ChatComponentText(MessageUtils.nebSignature+MessageUtils.startupMsg));
		}
	}
	public static void setApiKey(String key) {
		try {
			FileWriter fw;
			fw = new FileWriter(apiTextFile);
			fw.write(key);
			fw.close();
			
			NotEnoughBazaar.apiKey = key;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
