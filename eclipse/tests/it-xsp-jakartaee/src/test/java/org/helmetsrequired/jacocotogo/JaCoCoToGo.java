/**
 * Copyright (C) 2013 Matthew C. Jenkins (matt@helmetsrequired.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.helmetsrequired.jacocotogo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * <p>
 * JaCoCoToGo class.
 * </p>
 *
 * @author Matthew C. Jenkins
 */
@SuppressWarnings("nls")
public class JaCoCoToGo {

	private static final Logger logger = Logger.getLogger(JaCoCoToGo.class.getPackage().getName());
	private static final int MAX_PORT = (int) (Math.pow(2, 16) - 1);

	/**
	 * <p>
	 * fetchJaCoCoDataOverTcp.
	 * </p>
	 *
	 * @param hostname        the hostname where the remote jvm is running
	 * @param port            the port where the JaCoCo java agent TCP Server is
	 *                        listening
	 * @param outputFile      a {@link java.io.File} where the retrieved jacoco data
	 *                        should be written.
	 * @param resetAfterFetch whether the jacoco data on the remote system
	 *                        should be reset after fetching.
	 */
	public static final void fetchJaCoCoDataOverTcp(String hostname, int port, Path outputFile, boolean resetAfterFetch) {
		InetAddress hostAddress = checkHostname(hostname);
		checkPort(port);

		// fetch the execution data
		byte[] executionData = getExecutionDataViaJaCoCoTCPServer(hostAddress, port, resetAfterFetch);

		// save to file
		saveExecutionData(executionData, outputFile);
	}

	private static void saveExecutionData(byte[] executionData, Path outputFile) {
		if(logger.isLoggable(Level.INFO)) {
			logger.info(MessageFormat.format("Saving JaCoCo execution data to file: \"{0}\"", outputFile));
		}
		Path outputFileDir = outputFile.toAbsolutePath().getParent();
		if (!Files.exists(outputFileDir)) {
			try {
				Files.createDirectories(outputFileDir);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		if (executionData == null) {
			if(logger.isLoggable(Level.WARNING)) {
				logger.warning("executionData is null, nothing to save");
			}
			return;
		}
		try(OutputStream os = Files.newOutputStream(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			os.write(executionData);
		} catch (IOException ex) {
			throw new UncheckedIOException("Error saving execution data to file: " + outputFile, ex);
		}
	}
	
	private static InetAddress checkHostname(String hostname) {
		try {
			if(logger.isLoggable(Level.FINER)) {
				logger.finer(MessageFormat.format("Verifying that hostname: \"{0}\" can be resolved.", hostname));
			}
			return InetAddress.getByName(hostname);
		} catch (UnknownHostException ex) {
			throw new IllegalStateException("Unable to resolve hostname: '" + hostname + "'", ex);
		}
	}

	private static void checkPort(int port) {
		if (port < 1 || port > MAX_PORT) {
			throw new IllegalArgumentException("Invalid port: '" + port + "'");
		}
	}

	/**
	 *
	 * @param hostname        the hostname where the remote jvm is running.
	 * @param port            the port where the JaCoCo Java Agent TCP Server is
	 *                        listening.
	 * @param resetAfterFetch whether JaCoCo coverage data should be reset after
	 *                        fetch
	 * @return a byte array containing the JaCoCo execution data.
	 */
	private static byte[] getExecutionDataViaJaCoCoTCPServer(InetAddress address, int port, boolean resetAfterFetch) {
		ByteArrayOutputStream output = null;
		Socket socket = null;
		try {
			// 1. Open socket connection
			socket = new Socket(address, port);
			if(logger.isLoggable(Level.INFO)) {
				logger.info(MessageFormat.format("Connecting to {0}", socket.getRemoteSocketAddress()));
			}
			RemoteControlWriter remoteWriter = new RemoteControlWriter(socket.getOutputStream());
			RemoteControlReader remoteReader = new RemoteControlReader(socket.getInputStream());

			output = new ByteArrayOutputStream();
			ExecutionDataWriter outputWriter = new ExecutionDataWriter(output);
			remoteReader.setSessionInfoVisitor(outputWriter);
			remoteReader.setExecutionDataVisitor(outputWriter);

			// 2. Request dump
			remoteWriter.visitDumpCommand(true, resetAfterFetch);
			remoteReader.read();

			// 3. verify valid JaCoCo execution data
			byte[] outputBytes = output.toByteArray();
			if (outputBytes.length <= 5) {
				throw new IllegalStateException("No JaCoCo execution data received.");
			}

			// 4. Return data
			return outputBytes;
		} catch (final IOException e) {
			throw new UncheckedIOException("Unable to dump coverage data", e);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ex) {
					// bummer
				}
			}
			if (output != null) {
				try {
					output.close();
				} catch (IOException ex) {
					// bummer
				}
			}
		}
	}
}