/*
 * Copyright 2022 ICCS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.ntua.ece.cslab.iwnet.bda.analyticsml;


public class RunnerTest {
	/*AnalyticsInstance instance;

	@Before
	public void setUp() throws Exception {
		String fs_string = "jdbc:postgresql://bda-postgres:5432/test_db";
		String uname = "iwnet";
		String passwd = "123456";
		instance = new AnalyticInstance(fs_string, uname, passwd);
		instance.getEngineCatalog().addNewExecutEngine(1, "python3",
				"/usr/bin/python3", true, new JSONObject());
		instance.getKpiCatalog().addNewKpi(1, "recipe",
				"recipe", 1,
				 new JSONObject("{\"intarg\" : 1, \"strarg\" : \"str\"}"),
				"/code/examples/recipe.py");

	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void test() {
		//instance.run(1, "\"{\"ena\":1,\"duo\":2}\"");
	}*/
	//@Test
	//public void test()
	//{
		/*AnalyticsInternal mySystem = new AnalyticsInstance();
		ExecutEngineCatalog executEngineCatalog = ExecutEngineCatalog.getInstance();
		KpiCatalog kpiCatalog = mySystem.getKpiCatalog();

		System.out.println("KPI primitives: " + executables);
		KpiFactory kpiFactory = mySystem.getKpiFactory();

		try {
			executEngineCatalog.addNewExecutEngine("python3", "python3");
			System.out.println(executEngineCatalog.getAllExecutEngines());
			List<String> argtypes = Arrays.asList();
			executableCatalog.addNewExecutable(argtypes, executEngineCatalog.getExecutEngine(0), "./bin/kpi_null.py",
					"This calculates 0");
			executables = executableCatalog.getAllExecutables();

			System.out.println("KPI binaries: " + executables);
			assert (!executables.equals("{}"));

			// add new kpi
			int newKpiID = mySystem.getKpiCatalog().getKpiCounter();
			assert (newKpiID == 0);
			int executableID = mySystem.getExecutableCatalog().getExecutableCounter();
			assert (executableID == 1);

			executableID = 0;
			List<String> arguments = Arrays.asList("trucks", "amount of shit");
			String description = "This calculates shit done by blue trucks...";
			Kpi newKpi = kpiFactory.getKpiByExecutable(newKpiID, executableID, arguments, description);

			String kpis = kpiCatalog.getAllKpis();
			assert (kpis.equals("{}"));

			System.out.println("KPIs: " + kpis);
			kpiCatalog.addNewKpi(arguments, description, newKpi.getKpiInfo().getExecutable());
			kpis = kpiCatalog.getAllKpis();
			assert (!kpis.equals("{}"));
			Kpi kpi = kpiFactory.getKpiById(0);

			System.out.println("KPIs: " + kpis);
			// kpi.calculate();
			(new Thread(kpi)).start();
			try {
				Thread.sleep(3000);
				// System.out.println("Out");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println("Output: "+kpi.calculate());

			/*
			 * ArrayList<ArrayList<Double>> dataset = new ArrayList<ArrayList<Double>>();
			 * newModel.train(0, "Dataset1", new ArrayList<String>(), dataset, new
			 * ArrayList<String>());
			 * 
			 * System.out.println("Model catalog has " +
			 * MLSystem.getInstance().getModelCatalog().getModelNumber());
			 * Thread.sleep(5000);
			 * 
			 * DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			 * System.out.println("Model 0 last trained at " +
			 * dateFormat.format(MLSystem.getInstance().getModelCatalog().getModel(0).
			 * getTimestamp())); Model retModel = modelFactory.getModel(0);
			 * retModel.retrain(dataset, new ArrayList<String>());
			 * System.out.println("Model 0 last trained at " +
			 * dateFormat.format(MLSystem.getInstance().getModelCatalog().getModel(0).
			 * getTimestamp()));
			 */
		/*} catch (Exception e) {
			e.printStackTrace();
		}*/
	//}

}
