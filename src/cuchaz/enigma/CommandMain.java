package cuchaz.enigma;

import java.io.File;
import java.io.FileReader;
import java.util.jar.JarFile;

import cuchaz.enigma.Deobfuscator.ProgressListener;
import cuchaz.enigma.mapping.Mappings;
import cuchaz.enigma.mapping.MappingsReader;

public class CommandMain {
	
	public static class ConsoleProgressListener implements ProgressListener {
		
		private static final int ReportTime = 5000; // 5s

		private int m_totalWork;
		private long m_startTime;
		private long m_lastReportTime;
		
		@Override
		public void init(int totalWork, String title) {
			m_totalWork = totalWork;
			m_startTime = System.currentTimeMillis();
			m_lastReportTime = m_startTime;
			System.out.println(title);
		}

		@Override
		public void onProgress(int numDone, String message) {
			
			long now = System.currentTimeMillis();
			boolean isLastUpdate = numDone == m_totalWork;
			boolean shouldReport = isLastUpdate || now - m_lastReportTime > ReportTime;
			
			if (shouldReport) {
				int percent = numDone*100/m_totalWork;
				System.out.println(String.format("\tProgress: %3d%%", percent));
				m_lastReportTime = now;
			}
			if (isLastUpdate) {
				double elapsedSeconds = (now - m_startTime)/1000;
				System.out.println(String.format("Finished in %.1f seconds", elapsedSeconds));
			}
		}
	}
	
	public static void main(String[] args)
	throws Exception {
		
		try {
			
			// process the command
			String command = getArg(args, 0, "command");
			if (command.equalsIgnoreCase("deobfuscate")) {
				deobfuscate(args);
			} else if(command.equalsIgnoreCase("decompile")) {
				decompile(args);
			} else {
				throw new IllegalArgumentException("Command not recognized: " + command);
			}
		} catch (IllegalArgumentException ex) {
			System.out.println(ex.getMessage());
			printHelp();
		}
	}

	private static void printHelp() {
		System.out.println(String.format("%s - %s", Constants.Name, Constants.Version));
		System.out.println("Usage:");
		System.out.println("\tjava -cp enigma.jar cuchaz.enigma.CommandMain <command>");
		System.out.println("\twhere <command> is one of:");
		System.out.println("\t\tdeobfuscate <mappings file> <in jar> <out jar>");
		System.out.println("\t\tdecompile <mappings file> <in jar> <out folder>");
	}
	
	private static void decompile(String[] args)
	throws Exception {
		File fileMappings = getReadableFile(getArg(args, 1, "mappings file"));
		File fileJarIn = getReadableFile(getArg(args, 2, "in jar"));
		File fileJarOut = getWritableFolder(getArg(args, 3, "out folder"));
		Deobfuscator deobfuscator = getDeobfuscator(fileMappings, new JarFile(fileJarIn));
		deobfuscator.writeSources(fileJarOut, new ConsoleProgressListener());
	}

	private static void deobfuscate(String[] args)
	throws Exception {
		File fileMappings = getReadableFile(getArg(args, 1, "mappings file"));
		File fileJarIn = getReadableFile(getArg(args, 2, "in jar"));
		File fileJarOut = getWritableFile(getArg(args, 3, "out jar"));
		Deobfuscator deobfuscator = getDeobfuscator(fileMappings, new JarFile(fileJarIn));
		deobfuscator.writeJar(fileJarOut, new ConsoleProgressListener());
	}
	
	private static Deobfuscator getDeobfuscator(File fileMappings, JarFile jar)
	throws Exception {
		System.out.println("Reading mappings...");
		Mappings mappings = new MappingsReader().read(new FileReader(fileMappings));
		System.out.println("Reading jar...");
		Deobfuscator deobfuscator = new Deobfuscator(jar);
		deobfuscator.setMappings(mappings);
		return deobfuscator;
	}
	
	private static String getArg(String[] args, int i, String name) {
		if (i >= args.length) {
			throw new IllegalArgumentException(name + " is required");
		}
		return args[i];
	}

	private static File getWritableFile(String path) {
		File file = new File(path).getAbsoluteFile();
		File dir = file.getParentFile();
		if (dir == null || !dir.exists()) {
			throw new IllegalArgumentException("Cannot write to folder: " + file);
		}
		return file;
	}

	private static File getWritableFolder(String path) {
		File dir = new File(path).getAbsoluteFile();
		if (!dir.exists()) {
			throw new IllegalArgumentException("Cannot write to folder: " + dir);
		}
		return dir;
	}
	
	private static File getReadableFile(String path) {
		File file = new File(path).getAbsoluteFile();
		if (!file.exists()) {
			throw new IllegalArgumentException("Cannot find file: " + file.getAbsolutePath());
		}
		return file;
	}
}