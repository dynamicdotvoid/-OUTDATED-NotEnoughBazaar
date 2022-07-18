package dev.nulloverload.notenoughbazaar;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import org.json.JSONTokener;

import dev.nulloverload.notenoughbazaar.commands.NotEnoughBazaarCommand;
import dev.nulloverload.notenoughbazaar.handlers.FileHandler;
import dev.nulloverload.notenoughbazaar.handlers.GuiHandler;
import dev.nulloverload.notenoughbazaar.handlers.OrderListHandler;
import dev.nulloverload.notenoughbazaar.handlers.TickHandler;
import dev.nulloverload.notenoughbazaar.utilities.UpdateUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = NotEnoughBazaar.MODID, version = NotEnoughBazaar.VERSION)
public class NotEnoughBazaar
{
    public static final String MODID = "nullnotenoughbazaar";
    public static final String VERSION = "1.3.3";
    
    public static String apiKey = "";
    public static boolean inBazaar = false;
    public static boolean inConfirmation = false;
    public static IInventory confirmChest = null;
    public static JSONObject bazaarConversions = new JSONObject(
    	      new JSONTokener(NotEnoughBazaar.class.getResourceAsStream("/bazaarConversions.json")));
    public static JSONObject bazaarConversionsReversed = new JSONObject(
    	      new JSONTokener(NotEnoughBazaar.class.getResourceAsStream("/bazaarConversionsReversed.json")));
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	ClientCommandHandler.instance.registerCommand(new NotEnoughBazaarCommand());
    	MinecraftForge.EVENT_BUS.register(new OrderListHandler());
    	MinecraftForge.EVENT_BUS.register(new FileHandler());
    	MinecraftForge.EVENT_BUS.register(new GuiHandler());
    	MinecraftForge.EVENT_BUS.register(new TickHandler());
    }
    @EventHandler
    public void postinit(FMLPostInitializationEvent event)
    {
		Timer t = new Timer( );
		t.scheduleAtFixedRate(new TimerTask() {

		    @Override
		    public void run() {
		      if(Minecraft.getMinecraft().thePlayer!=null&&apiKey!="") {
		    	  UpdateUtils.checkOrderUpdate();
		      }

		    }
		}, 0,700);
    }
}
