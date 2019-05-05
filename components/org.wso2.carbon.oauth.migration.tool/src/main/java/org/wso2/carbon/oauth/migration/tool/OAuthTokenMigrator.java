/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.oauth.migration.tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oauth.migration.config.SystemConfig;
import org.wso2.carbon.oauth.migration.runtime.OAuthMigrationExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.util.Objects;

public class OAuthTokenMigrator {

    private static final Logger log = LoggerFactory.getLogger(OAuthTokenMigrator.class);

    private static final String CMD_OPTION_CONFIG_DIR = "d";
    private static final String CMD_OPTION_CONFIG_CARBON_HOME = "carbon";
    private static final String CMD_OPTION_HELP = "help";
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String CONF_DIRECTORY = "/conf";
    private static final String CARBON_HOME = "CARBON_HOME";

    public static void main(String[] args) throws Exception {

        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

        Options options = new Options();

        options.addOption(CMD_OPTION_CONFIG_DIR, true, "Directory where config.json file located (mandatory)");
        options.addOption(CMD_OPTION_HELP, false, "Help");
        options.addOption(CMD_OPTION_CONFIG_CARBON_HOME, true, "Carbon Home (optional)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption(CMD_OPTION_HELP)) {
            emitHelp(System.out);
            return;
        }

        String homeDir;
        if (cmd.hasOption(CMD_OPTION_CONFIG_DIR)) {
            homeDir = cmd.getOptionValue(CMD_OPTION_CONFIG_DIR);
        } else {
            homeDir = Paths.get(System.getProperty("user.dir")).getParent().toString() + CONF_DIRECTORY;
        }

        OAuthTokenMigrator tokenMigrator = new OAuthTokenMigrator();
        tokenMigrator.process(homeDir);
    }

    private void process(String homeDir)
            throws OAuthMigrationExecutionException {

        ConfigReader configReader = ConfigReader.getInstance();
        try {
            File home = new File(homeDir).getAbsoluteFile().getCanonicalFile();
            SystemConfig systemConfig = configReader.readSystemConfig(new File(home, CONFIG_FILE_NAME));
            systemConfig.setWorkDir(home.toPath());
            OAuthMigrationExecutor executor = new OAuthMigrationExecutor(systemConfig);
            executor.execute();
        } catch (IOException e) {
            throw new OAuthMigrationExecutionException("Could not load config from directory: " + homeDir, e, "E_INIT", null);
        }
    }

    private static void emitHelp(PrintStream out) {

        ByteBuffer buffer = ByteBuffer.allocateDirect(512);
        try (InputStream inputStream = OAuthTokenMigrator.class.getClassLoader().getResourceAsStream("help.md");
             ReadableByteChannel readableByteChannel = Channels.newChannel(Objects.requireNonNull(inputStream));
             WritableByteChannel writableByteChannel = Channels.newChannel(out) ) {
            while (readableByteChannel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    writableByteChannel.write(buffer);
                }
                buffer.clear();
            }
        } catch (IOException e) {
            log.error("Could not read the help file.");
        }
    }
}
