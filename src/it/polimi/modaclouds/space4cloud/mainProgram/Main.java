/*******************************************************************************
 * Copyright 2014 Giovanni Paolo Gibilisco
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * 
 */
package it.polimi.modaclouds.space4cloud.mainProgram;

import java.io.File;

import javax.swing.UIManager;

/**
 * Main class of the CSPACE tool.<br/>
 * Workflow:
 * <ol>
 * <li>Load the basic Resource Model through the application window LoadModel</li>
 * <li>
 * Complete the Resource Model using the ResourceContainerSelection:
 * <ul>
 * <li>Select a Cloud Element for each Resource Container using the application
 * window CloudResourceSelection.
 * <ul>
 * <li>Specify an Allocation Profile for the Resource Container using the
 * application windows AllocationProfileSpecification</li>
 * <li>Modify the derived Processing Resources through the application window
 * CloudResourceSpecification</li>
 * </ul>
 * </li>
 * <li>Specify an Efficiency Profile for each Processing Resource of each
 * Resource Container through the application windows
 * EfficiencyProfileSpecification.</li>
 * <li>Derive the 24 Resource Models.</li>
 * <li>Load an Allocation Model through the application window LoadModel.</li>
 * <li>Derive the 24 Allocation Models.</li>
 * <li>Derive costs.</li>
 * </ul>
 * </li>
 * <li>Load a Usage Model through the application window LoadModel.</li>
 * <li>Specify the Usage Profile through the application window
 * UsageProfileSpecification.</li>
 * <li>Derive the 24 Usage Models.</li>
 * </ol>
 * 
 * @author Michele Ciavotta, Giovanni Gibilisco
 * 
 */

public class Main {
	
	public static void perform(String... args) {
		String configuration = null;
		if (args != null && args.length > 0)
			configuration = args[0];
		
		Space4Cloud instance;
		if (configuration != null && new File(configuration).exists())
			instance = new Space4Cloud(configuration);
		else 
			instance = new Space4Cloud();
		instance.start();
	}
	
	public static void main(String[] args) {
		try {
//            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		perform(args);
	}

}
