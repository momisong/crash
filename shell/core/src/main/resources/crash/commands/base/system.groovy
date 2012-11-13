package crash.commands.base

import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.spi.Completer
import org.crsh.cmdline.ParameterDescriptor
import org.crsh.cmdline.annotations.Option

import org.crsh.cmdline.completers.EnumCompleter
import org.crsh.cmdline.spi.Completion
import java.util.regex.Pattern
import org.crsh.cmdline.annotations.Required

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@Usage("vm system properties commands")
class system extends CRaSHCommand implements Completer {

  // Properties command

  @Usage("list the vm system properties")
  @Command
  public void propls(
    InvocationContext<Map> context,
    @Usage("filter the property with a regular expression on their name")
    @Option(names=["f","filter"])
    String filter) {
    def pattern = Pattern.compile(filter?:".*");
    System.getProperties().each { key, value ->
      def matcher = key =~ pattern;
      if (matcher.matches()) {
        try {
          context.provide([NAME: key, VALUE: value] as LinkedHashMap)
        }
        catch (IOException e) {
          e.printStackTrace()
        };
      }
    }
  }

  @Usage("set a system property")
  @Command
  public void propset(@PropertyName @Required String name, @PropertyValue @Required String value) {
    System.setProperty name.toString(), value
  }

  @Usage("get a system property")
  @Command
  public String propget(@PropertyName @Required String name) {
    return System.getProperty(name.toString()) ?: ""
  }

  @Usage("remove a system property")
  @Command
  public void proprm(@PropertyName @Required String name) {
    System.clearProperty name.toString()
  }

  Completion complete(ParameterDescriptor<?> parameter, String prefix)
  {
    def b = new Completion.Builder(prefix);
    if (parameter.getAnnotation().annotationType().equals(PropertyName.class)) {
      System.getProperties().each() {
        if (it.key.startsWith(prefix)) {
          b.add(it.key.substring(prefix.length()), true)
        }
      }
    }
    return b.build();
  }

  // Memory commands

  @Usage("call garbage collector")
  @Command
  public void gc() {
    System.gc()
  }

  @Usage("show free memory")
  @Command
  public String freemem(@UnitOpt Unit unit, @DecimalOpt Integer decimal) {
    if (unit == null) {
      unit = Unit.B
    }
    return unit.compute(Runtime.getRuntime().freeMemory(), decimal) + unit.human
  }

  @Usage("show total memory")
  @Command
  public String totalmem(@UnitOpt Unit unit, @DecimalOpt Integer decimal) {
    if (unit == null) {
      unit = Unit.B;
    }
    return unit.compute(Runtime.getRuntime().totalMemory(), decimal) + unit.human
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the property name")
@Man("The name of the property")
@Argument(name = "name")
@interface PropertyName { }

@Retention(RetentionPolicy.RUNTIME)
@Usage("the property value")
@Man("The value of the property")
@Argument(name = "value")
@interface PropertyValue { }

enum Unit { B(1, "b"), K(1024, "Kb"), M(1024 * 1024, "Mb"), G(1024 * 1024 * 1024, "Gb")

  final long unit
  final String human

  Unit(long unit, String human) {
    this.unit = unit
    this.human = human
  }

  public String compute(long space, Integer decimal) {
    if (decimal == null) {
      decimal = 0
    }
    return new BigDecimal(space / unit).setScale(decimal, BigDecimal.ROUND_HALF_UP).toPlainString()
  }

  public String getHuman() {
    return this.human
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Usage("the unit of the memory space size")
@Man("The unit of the memory space size {(B)yte, (O)ctet, (M)egaOctet, (G)igaOctet}")
@Option(names=["u","unit"],completer=EnumCompleter)
@interface UnitOpt { }

@Retention(RetentionPolicy.RUNTIME)
@Usage("number of decimal")
@Man("The number of decimal (default 0)")
@Option(names=["d","decimal"])
@interface DecimalOpt { }
