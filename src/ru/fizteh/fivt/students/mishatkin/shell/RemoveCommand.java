package ru.fizteh.fivt.students.mishatkin.shell;
/**
 * RemoveCommand.java
 * Created by Vladimir Mishatkin on 9/25/13
 */
public class RemoveCommand extends Command {
	RemoveCommand(CommandReceiver receiver) {
		super(receiver);
		type = CommandType.RM;
	}

	@Override
	public void execute() throws ShellException {
		receiver.removeCommand(args[0]);
	}
}
