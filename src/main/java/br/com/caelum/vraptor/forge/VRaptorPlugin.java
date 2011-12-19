package br.com.caelum.vraptor.forge;

import javax.inject.Inject;

import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;

public @Alias("vraptor")
class VRaptorPlugin implements org.jboss.forge.shell.plugins.Plugin {

	@Inject
	private ShellPrompt prompt;

	@DefaultCommand
	public void exampleDefaultCommand(@Option String opt, PipeOut out) {
		out.println(">> invoked default command with option value: " + opt);
		// this method will be invoked, and 'opt' will be passed from the
		// command line
		// 'out' is your handle to this plugin's output stream.
	}

	public @Command("run")
	void run(PipeOut out, @Option(name = "value") final String arg) {
		System.out.println("Executed default command with value: " + arg);
	}
}