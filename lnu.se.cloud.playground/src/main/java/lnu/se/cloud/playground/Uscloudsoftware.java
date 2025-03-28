package lnu.se.cloud.playground;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;
import org.cloudsimplus.vms.Vm;

public class Uscloudsoftware {

	private final CloudSimPlus simulation;
	private final Datacenter datacenter;
	private final DatacenterBroker broker;
	private ArrayDeque<User> users;
	private final int USER_TYPES = 2;
	private final ActivityNode[] userActivityGraphs = new ActivityNode[USER_TYPES];
	private static ArrayList<LinkedCloudlet> cloudletList;
	private static ArrayList<NamedHost> hostRecap;

	public static void main(String[] args) {
		List<SimulationResult> simResults = new ArrayList<>(Utils.REPETITIONS);
		for(int i = 0; i < Utils.REPETITIONS; i++) {
			new Uscloudsoftware();
			simResults.add(new SimulationResult(cloudletList, hostRecap));
		}
		for(String interaction : simResults.get(0).interactionResults.stream().map(i -> i.interactionName).distinct().collect(Collectors.toList())) {
			System.out.println("- " + interaction + " ->");
			System.out.println("SCI: " + simResults.stream().collect(Collectors.summarizingDouble(r -> r.interactionResults.stream().filter(ir -> ir.interactionName.equals(interaction)).findFirst().get().avgSCI)).getAverage());
			System.out.println("Service Time: " + simResults.stream().collect(Collectors.summarizingDouble(r -> r.interactionResults.stream().filter(ir -> ir.interactionName.equals(interaction)).findFirst().get().avgServiceTime)).getAverage());
			System.out.println("Throughput: " + simResults.stream().collect(Collectors.summarizingLong(r -> r.interactionResults.stream().filter(ir -> ir.interactionName.equals(interaction)).findFirst().get().throughput)).getAverage());
		}
		System.out.println("------------------");
		for(String device : hostRecap.stream().map(NamedHost::getName).distinct().collect(Collectors.toList())) {
			System.out.println("- " + device + " ->");
			System.out.println("SCI: " + simResults.stream().collect(Collectors.summarizingDouble(r -> r.deviceResults.stream().filter(dr -> dr.deviceName.equals(device)).findFirst().get().avgSCI)).getAverage());
			System.out.println("Service Time: " + simResults.stream().collect(Collectors.summarizingDouble(r -> r.deviceResults.stream().filter(dr -> dr.deviceName.equals(device)).findFirst().get().avgServiceTime)).getAverage());
			System.out.println("Throughput: " + simResults.stream().collect(Collectors.summarizingLong(r -> r.deviceResults.stream().filter(dr -> dr.deviceName.equals(device)).findFirst().get().throughput)).getAverage());
			System.out.println("Utilization: " + simResults.stream().collect(Collectors.summarizingDouble(r -> r.deviceResults.stream().filter(dr -> dr.deviceName.equals(device)).findFirst().get().avgUtilization)).getAverage());
			System.out.println("Dropped Connections: " + simResults.stream().collect(Collectors.summarizingLong(r -> r.deviceResults.stream().filter(dr -> dr.deviceName.equals(device)).findFirst().get().droppedConnections)).getAverage());
		}
	}

	private Uscloudsoftware() {
		simulation = new CloudSimPlus();

		final ArrayList<NamedHost> hostList = new ArrayList<>(9);
		final ArrayList<Vm> vmList = new ArrayList<>(13);
		cloudletList = new ArrayList<>();
		NamedHost host;

		host = new NamedHost("FrontendServer", 65536, 20000, 5000000, List.of(new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000)), 1350000.0, 1.892E8, 205.0, "us_east_1");
		hostList.add(host);
		Vm FrontendServerNodeJsPod = new LinkedVm(9000, 16, host.getName()).setRam(16384).setBw(1000).setSize(10000).setDescription("NodeJsPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(FrontendServerNodeJsPod);
		host = new NamedHost("ShippingServer", 65536, 20000, 5000000, List.of(new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000)), 1350000.0, 1.892E8, 205.0, "us_east_1");
		hostList.add(host);
		Vm ShippingServerJavaPod = new LinkedVm(9000, 2, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("JavaPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(ShippingServerJavaPod);
		host = new NamedHost("OrdersServer", 65536, 20000, 5000000, List.of(new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000)), 1350000.0, 1.892E8, 205.0, "us_east_1");
		hostList.add(host);
		Vm OrdersServerJavaPod = new LinkedVm(9000, 4, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("JavaPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(OrdersServerJavaPod);
		Vm OrdersServerMongoDBPod = new LinkedVm(9000, 2, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("MongoDBPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(OrdersServerMongoDBPod);
		host = new NamedHost("CartsServer", 65536, 20000, 5000000, List.of(new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000)), 1350000.0, 1.892E8, 205.0, "us_east_1");
		hostList.add(host);
		Vm CartsServerJavaPod = new LinkedVm(9000, 2, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("JavaPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(CartsServerJavaPod);
		Vm CartsServerMongoDBPod = new LinkedVm(9000, 2, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("MongoDBPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(CartsServerMongoDBPod);
		host = new NamedHost("CatalogueServer", 65536, 20000, 5000000, List.of(new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000)), 1350000.0, 1.892E8, 205.0, "us_east_1");
		hostList.add(host);
		Vm CatalogueServerGoPod = new LinkedVm(9000, 2, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("GoPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(CatalogueServerGoPod);
		Vm CatalogueServerMariaDBPod = new LinkedVm(9000, 2, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("MariaDBPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(CatalogueServerMariaDBPod);
		host = new NamedHost("PaymentServer", 65536, 20000, 5000000, List.of(new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000)), 1350000.0, 1.892E8, 205.0, "us_east_1");
		hostList.add(host);
		Vm PaymentServerGoPod = new LinkedVm(9000, 2, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("GoPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(PaymentServerGoPod);
		host = new NamedHost("QueueMasterServer", 65536, 20000, 5000000, List.of(new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000)), 1350000.0, 1.892E8, 205.0, "us_east_1");
		hostList.add(host);
		Vm QueueMasterServerJavaPod = new LinkedVm(9000, 2, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("JavaPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(QueueMasterServerJavaPod);
		host = new NamedHost("RabbitMQServer", 65536, 20000, 5000000, List.of(new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000)), 1350000.0, 1.892E8, 205.0, "us_east_1");
		hostList.add(host);
		Vm RabbitMQServerRabbitMQPod = new LinkedVm(9000, 2, host.getName()).setRam(512).setBw(1000).setSize(10000).setDescription("RabbitMQPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(RabbitMQServerRabbitMQPod);
		host = new NamedHost("UsersServer", 65536, 20000, 5000000, List.of(new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000), new PeSimple(9000)), 1350000.0, 1.892E8, 205.0, "us_east_1");
		hostList.add(host);
		Vm UsersServerGoPod = new LinkedVm(9000, 2, host.getName()).setRam(8192).setBw(1000).setSize(10000).setDescription("GoPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(UsersServerGoPod);
		Vm UsersServerMongoDBPod = new LinkedVm(9000, 2, host.getName()).setRam(8192).setBw(1000).setSize(10000).setDescription("MongoDBPod").setCloudletScheduler(new CloudletSchedulerSpaceShared());
		vmList.add(UsersServerMongoDBPod);
		
		datacenter = new DatacenterSimple(simulation, hostList, new CustomAllocationPolicy(hostList));
		datacenter.setName("Uscloudsoftware");

		broker = new DatacenterBrokerSimple(simulation);
		broker.submitVmList(vmList);

		Map<String, TriConsumer<EventInfo, String, BiConsumer<EventInfo, String>>> interactions = new HashMap<>();
		interactions.put("Nop", (evt, user, next) -> {next.accept(evt, user);});
		interactions.put("Sink", (evt, user, next) -> {});

		interactions.put("Login", (evt, user, next) -> {
			LinkedCloudlet cl;
			ArrayList<LinkedCloudlet> newCloudlets = new ArrayList<>(3);
			cl = new LinkedCloudlet(4500, 1, new UtilizationModelFull(), "LoginAttempt", FrontendServerNodeJsPod, user, "Login");
			cl.setSubmissionDelay(0.0);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			if(next != null) {
				cl.addOnFinishListener(evtt -> next.accept(evtt, user));
			}
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(3000, 1, new UtilizationModelFull(), "UsersServiceExecution", UsersServerGoPod, user, "Login");
			cl.setSubmissionDelay(0.1);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(2000, 1, new UtilizationModelFull(), "UsersDatabaseQuery", UsersServerMongoDBPod, user, "Login");
			cl.setSubmissionDelay(0.2);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			broker.submitCloudletList(newCloudlets);
			for(LinkedCloudlet cloudlet : newCloudlets) {
				broker.bindCloudletToVm(cloudlet, cloudlet.getRelatedVm());
			}
			cloudletList.addAll(newCloudlets);
		});

		interactions.put("AddToCart", (evt, user, next) -> {
			LinkedCloudlet cl;
			ArrayList<LinkedCloudlet> newCloudlets = new ArrayList<>(3);
			cl = new LinkedCloudlet(3000, 1, new UtilizationModelFull(), "AddToCartButtonPress", FrontendServerNodeJsPod, user, "AddToCart");
			cl.setSubmissionDelay(0.0);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			if(next != null) {
				cl.addOnFinishListener(evtt -> next.accept(evtt, user));
			}
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(2000, 1, new UtilizationModelFull(), "CartsServiceExecution", CartsServerJavaPod, user, "AddToCart");
			cl.setSubmissionDelay(0.1);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(1000, 1, new UtilizationModelFull(), "AddItemToCartQuery", CartsServerMongoDBPod, user, "AddToCart");
			cl.setSubmissionDelay(0.2);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			broker.submitCloudletList(newCloudlets);
			for(LinkedCloudlet cloudlet : newCloudlets) {
				broker.bindCloudletToVm(cloudlet, cloudlet.getRelatedVm());
			}
			cloudletList.addAll(newCloudlets);
		});

		interactions.put("DisplayCatalogue", (evt, user, next) -> {
			LinkedCloudlet cl;
			ArrayList<LinkedCloudlet> newCloudlets = new ArrayList<>(3);
			cl = new LinkedCloudlet(3000, 1, new UtilizationModelFull(), "IndexPageLoading", FrontendServerNodeJsPod, user, "DisplayCatalogue");
			cl.setSubmissionDelay(0.0);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			if(next != null) {
				cl.addOnFinishListener(evtt -> next.accept(evtt, user));
			}
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(2000, 1, new UtilizationModelFull(), "CatalogueServiceExecution", CatalogueServerGoPod, user, "DisplayCatalogue");
			cl.setSubmissionDelay(0.1);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(1000, 1, new UtilizationModelFull(), "CatalogueDatabaseQuery", CatalogueServerMariaDBPod, user, "DisplayCatalogue");
			cl.setSubmissionDelay(0.2);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			broker.submitCloudletList(newCloudlets);
			for(LinkedCloudlet cloudlet : newCloudlets) {
				broker.bindCloudletToVm(cloudlet, cloudlet.getRelatedVm());
			}
			cloudletList.addAll(newCloudlets);
		});

		interactions.put("Checkout", (evt, user, next) -> {
			LinkedCloudlet cl;
			ArrayList<LinkedCloudlet> newCloudlets = new ArrayList<>(7);
			cl = new LinkedCloudlet(18000, 2, new UtilizationModelFull(), "PerformOrder", FrontendServerNodeJsPod, user, "Checkout");
			cl.setSubmissionDelay(0.0);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			if(next != null) {
				cl.addOnFinishListener(evtt -> next.accept(evtt, user));
			}
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(15000, 1, new UtilizationModelFull(), "ProcessNewOrder", OrdersServerJavaPod, user, "Checkout");
			cl.setSubmissionDelay(0.2);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(3000, 1, new UtilizationModelFull(), "ProcessPayment", PaymentServerGoPod, user, "Checkout");
			cl.setSubmissionDelay(0.3);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(1000, 1, new UtilizationModelFull(), "SendShipmentToQueue", ShippingServerJavaPod, user, "Checkout");
			cl.setSubmissionDelay(0.4);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(1000, 1, new UtilizationModelFull(), "AddShipmentToQueue", RabbitMQServerRabbitMQPod, user, "Checkout");
			cl.setSubmissionDelay(0.5);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(1000, 1, new UtilizationModelFull(), "StoreOrder", OrdersServerMongoDBPod, user, "Checkout");
			cl.setSubmissionDelay(0.6);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(2500, 1, new UtilizationModelFull(), "ProcessShipment", QueueMasterServerJavaPod, user, "Checkout");
			cl.setSubmissionDelay(0.6);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			broker.submitCloudletList(newCloudlets);
			for(LinkedCloudlet cloudlet : newCloudlets) {
				broker.bindCloudletToVm(cloudlet, cloudlet.getRelatedVm());
			}
			cloudletList.addAll(newCloudlets);
		});

		interactions.put("LoadCheckoutPage", (evt, user, next) -> {
			LinkedCloudlet cl;
			ArrayList<LinkedCloudlet> newCloudlets = new ArrayList<>(5);
			cl = new LinkedCloudlet(10000, 1, new UtilizationModelFull(), "LoadCheckoutInformation", FrontendServerNodeJsPod, user, "LoadCheckoutPage");
			cl.setSubmissionDelay(0.0);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			if(next != null) {
				cl.addOnFinishListener(evtt -> next.accept(evtt, user));
			}
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(2000, 1, new UtilizationModelFull(), "FetchUserInfo", UsersServerGoPod, user, "LoadCheckoutPage");
			cl.setSubmissionDelay(0.1);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(1000, 1, new UtilizationModelFull(), "QueryUserInfo", UsersServerMongoDBPod, user, "LoadCheckoutPage");
			cl.setSubmissionDelay(0.2);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(2000, 1, new UtilizationModelFull(), "FetchCartInfo", CartsServerJavaPod, user, "LoadCheckoutPage");
			cl.setSubmissionDelay(0.3);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			cl = new LinkedCloudlet(1000, 1, new UtilizationModelFull(), "QueryCartInfo", CartsServerMongoDBPod, user, "LoadCheckoutPage");
			cl.setSubmissionDelay(0.4);
			cl.setSizes(1024);
			cl.setUtilizationModelRam(new UtilizationModelDynamic(0.0001));
			cl.setUtilizationModelBw(new UtilizationModelDynamic(0.0001));
			newCloudlets.add(cl);
			broker.submitCloudletList(newCloudlets);
			for(LinkedCloudlet cloudlet : newCloudlets) {
				broker.bindCloudletToVm(cloudlet, cloudlet.getRelatedVm());
			}
			cloudletList.addAll(newCloudlets);
		});

		int graphsIndex = 0;

		ActivityNode UserANewConnection = new ActivityNode(interactions.get("Nop"));
		userActivityGraphs[graphsIndex++] = UserANewConnection;
		ActivityNode UserAPerformLogin = new ActivityNode(interactions.get("Login"));
		ActivityNode UserADoNotBuyAnything = new ActivityNode(interactions.get("Sink"));
		ActivityNode UserADecideIfCompletePurchase = new ActivityNode(interactions.get("Nop"));
		ActivityNode UserAOrderCompleted = new ActivityNode(interactions.get("Sink"));
		ActivityNode UserAConsiderAddingToCart = new ActivityNode(interactions.get("Nop"));
		ActivityNode UserAGoToCheckout = new ActivityNode(interactions.get("LoadCheckoutPage"));
		ActivityNode UserAShowCatalogue = new ActivityNode(interactions.get("DisplayCatalogue"));
		ActivityNode UserADecideIfGoToCheckout = new ActivityNode(interactions.get("Nop"));
		ActivityNode UserAGoToCatalogue = new ActivityNode(interactions.get("Nop"));
		ActivityNode UserAAddItemToCart = new ActivityNode(interactions.get("AddToCart"));
		ActivityNode UserADecideIfAddElement = new ActivityNode(interactions.get("Nop"));
		ActivityNode UserAFinalizePurchase = new ActivityNode(interactions.get("Checkout"));
		UserAConsiderAddingToCart.addChild(UserADecideIfAddElement);
		UserAFinalizePurchase.addChild(UserAOrderCompleted);
		UserADecideIfCompletePurchase.addChild(UserADoNotBuyAnything, 20);
		UserAGoToCatalogue.addChild(UserAShowCatalogue);
		UserAAddItemToCart.addChild(UserADecideIfGoToCheckout);
		UserADecideIfCompletePurchase.addChild(UserAGoToCatalogue, 40);
		UserAGoToCheckout.addChild(UserADecideIfCompletePurchase);
		UserADecideIfCompletePurchase.addChild(UserAFinalizePurchase, 40);
		UserADecideIfGoToCheckout.addChild(UserAConsiderAddingToCart, 35);
		UserAPerformLogin.addChild(UserAGoToCatalogue);
		UserANewConnection.addChild(UserAPerformLogin);
		UserAShowCatalogue.addChild(UserAConsiderAddingToCart);
		UserADecideIfAddElement.addChild(UserAAddItemToCart, 40);
		UserADecideIfGoToCheckout.addChild(UserAGoToCheckout, 65);
		UserADecideIfAddElement.addChild(UserADoNotBuyAnything, 60);

		ActivityNode UserBPerformLogin = new ActivityNode(interactions.get("Login"));
		ActivityNode UserBShowCartRecap = new ActivityNode(interactions.get("LoadCheckoutPage"));
		ActivityNode UserBAddElementToCart = new ActivityNode(interactions.get("AddToCart"));
		ActivityNode UserBDecideToAdd = new ActivityNode(interactions.get("Nop"));
		ActivityNode UserBNewConnection = new ActivityNode(interactions.get("Nop"));
		userActivityGraphs[graphsIndex++] = UserBNewConnection;
		ActivityNode UserBShowCatalogue = new ActivityNode(interactions.get("DisplayCatalogue"));
		ActivityNode UserBCompleteOrder = new ActivityNode(interactions.get("Checkout"));
		ActivityNode UserBDisconnection = new ActivityNode(interactions.get("Sink"));
		ActivityNode UserBDecideIfAddMore = new ActivityNode(interactions.get("Nop"));
		UserBPerformLogin.addChild(UserBShowCatalogue);
		UserBDecideIfAddMore.addChild(UserBDecideToAdd, 35);
		UserBDecideIfAddMore.addChild(UserBShowCartRecap, 65);
		UserBNewConnection.addChild(UserBPerformLogin);
		UserBShowCatalogue.addChild(UserBDecideToAdd);
		UserBDecideToAdd.addChild(UserBAddElementToCart);
		UserBCompleteOrder.addChild(UserBDisconnection);
		UserBShowCartRecap.addChild(UserBCompleteOrder);
		UserBAddElementToCart.addChild(UserBDecideIfAddMore);

		double tt = 0.0;
		int userId = 0;
		users = new ArrayDeque<User>();
		while(tt < Utils.EXEC_TIME) {
			User u = new User("User"+userId, tt);
			if(tt == 0.0) {
				this.randomUserType().process(u.getName());
			} else {
				users.add(u);
			}
			double iat = -Math.log(1 - Utils.rng.nextDouble()) / Utils.EXP_LAMBDA;
			tt += iat;
			userId++;
		}

		simulation.addOnClockTickListener(this::checkArrivals);
		simulation.terminateAt(Utils.EXEC_TIME);
		simulation.start();

		//new CustomTableBuilder(cloudletList, broker.getShutdownTime()).build();
		//final List<Vm> vmRecap = broker.getVmCreatedList();
		//new VmTableBuilder(vmRecap).build();
		hostRecap = hostList;
	}

	public void checkArrivals(EventInfo evt) {
		if(!users.isEmpty() && users.peek().getStartTime() <= evt.getTime()) {
			User user = users.pop();
			this.randomUserType().process(user.getName(), evt);
		}
	}

	private ActivityNode randomUserType() {
		return this.userActivityGraphs[Utils.rng.nextInt(USER_TYPES)];
	}
}
