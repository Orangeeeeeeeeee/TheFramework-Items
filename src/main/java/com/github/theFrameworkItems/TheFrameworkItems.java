package com.github.theFrameworkItems;

import com.github.theFrameworkItems.commands.GetCommand;
import com.github.theFrameworkItems.managers.ItemListener;
import com.github.theFrameworkItems.managers.ItemManager;
import me.mcplugins.theframework.TheFramework;
import me.mcplugins.theframework.commands.CommandsManager;
import me.mcplugins.theframework.managers.TextManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheFrameworkItems extends JavaPlugin {

	@Override
	public void onEnable() {
		if (!TheFramework.asPlugin().isEnabled()) {
			TextManager.console(TextManager.format("&c[The Framework] Items requires The Framework to be enabled!"));
			setEnabled(false);
		}

		TextManager.console(TextManager.format(
			"&b\n\n" +
				"  █ ▀█▀ █▀▀ █▀▄▀█ █▀\n" +
				"  █  █  ██▄ █ ▀ █ ▄█\n" +
				"              &3v&f1.0.0" +
				"\n"
		));

		for (Player player : Bukkit.getOnlinePlayers())
			ItemManager.reload(player);

		TheFramework.registerEvent(new ItemListener());
		CommandsManager.register(new GetCommand(), this);
	}

	@Override
	public void onDisable() {
		TextManager.console(TextManager.format("&c[The Framework] Items is offline."));
	}
}
