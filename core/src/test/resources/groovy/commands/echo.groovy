import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("Echo text")
public class select extends org.crsh.command.BaseCommand<Void, Node> {

  @Argument(usage = "the content")
  def List<String> arguments;

  public void execute(CommandContext<Void, Node> context) throws ScriptException {
    arguments.each { context.writer.print(it) }
  }
}