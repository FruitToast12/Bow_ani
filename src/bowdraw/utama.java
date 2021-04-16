package bowdraw;

import org.bukkit.plugin.java.JavaPlugin;

public class utama extends JavaPlugin {
	@Override
	public void onEnable() {
		getLogger().info("Enabled");
		getServer().getPluginManager().registerEvents(new bow(), this);
	}

	@Override
	public void onDisable() {
		getLogger().info("Plugin Disabled");
	}
}