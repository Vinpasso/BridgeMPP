package bridgempp.test.general;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class TheOneAndOnlyTest {

	boolean success = true;
	List<String> ignoredMethods = new LinkedList<>();

//	@Test
//	public void test() throws IllegalAccessException {
//		ignoredMethods.add("main");
//		ignoredMethods.add("shutdown");
//		ignoredMethods.add("exit");
//		
//		for(Method m :new Object().getClass().getMethods())
//		{
//			ignoredMethods.add(m.getName());
//		}
//		
//		
//		
//		getAllTestedClasses().forEach(c -> {
//			Object cObject = null;
//			try {
//				Constructor<?>[] constructors = c.getDeclaredConstructors();
//				for (Constructor<?> cc : constructors) {
//					Object[] parameters = fillParameters(cc.getParameters());
//					cc.setAccessible(true);
//					cObject = cc.newInstance(parameters);
//				}
//			}
//			catch (InvocationTargetException e)
//			{
//				System.out.println("Constructor error. class: " + c.getName());
//				e.getCause().printStackTrace();
//				success = false;
//				return;
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//			for (Method m : c.getDeclaredMethods()) {
//				if(ignoredMethods.contains(m.getName())) continue;
//				try {
//					Object[] parameters = fillParameters(m.getParameters());
//					m.setAccessible(true);
//					m.invoke(cObject, parameters);
//					System.out.println("Is null pointer safe:" + m.getName());
//				} catch (Exception e) {
//					System.err.println("Not null pointer safe:" + m.getName());
//					success = false;
//				}
//			}
//		});
////		if (!success) {
////			fail("Some Methods were not Null Pointer Safe!");
////		}
//	}

	private Object[] fillParameters(Parameter[] reflectionParameters) {
		Object[] parameters = new Object[reflectionParameters.length];
		for(int i = 0; i < reflectionParameters.length; i++)
		{
			if(reflectionParameters[i].getType().isPrimitive())
			{
				switch(reflectionParameters[i].getType().getName())
				{
				case "int":
					parameters[i] = new Random().nextInt();
					break;
				case "boolean":
					parameters[i] = (Math.random() < 0.5);
					break;
				case "long":
					parameters[i] = new Random().nextLong();
					break;
				default:
					System.out.println("Unknown primitive: " + reflectionParameters[i].getType().getName());
				}
			}
		}
		return parameters;
	}

	public List<Class<?>> getAllTestedClasses() {
		File file = new File("src/main/java/bridgempp/");
		List<Class<?>> classList = new LinkedList<>();
		getTestClassesFromPath(file.getAbsolutePath(), classList, "bridgempp");
		return classList;
	}

	public void getTestClassesFromPath(String path, List<Class<?>> list,
			String thePackage) {
		File directory = new File(path);
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				getTestClassesFromPath(file.getAbsolutePath(), list, thePackage
						+ "." + file.getName());
			} else if (file.getName().endsWith(".java")) {
				String className = file.getName().substring(0,
						file.getName().indexOf("."));
				try {
					list.add(Class.forName(thePackage + "." + className));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
