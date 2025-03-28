package lnu.se.cloud.playground;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicyAbstract;
import org.cloudsimplus.builders.tables.MarkdownTable;
import org.cloudsimplus.builders.tables.TableBuilderAbstract;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.utilizationmodels.UtilizationModel;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

class NamedHost extends HostSimple {
	private String name;
	private static final double[] UTIL_POINTS = {0, 0.10, 0.50, 1};
	private static final double[] TEADS_CURVE = {0.12, 0.32, 0.75, 1.02};
	private static final int DEFAULT_GRID_CARBON_INTENSITY = 300;
	private int gridCarbonIntensity;
	private static final double NETWORK_ENERGY = 0.0; // Usually small, simplified to 0 at the moment
	private double embodiedEmissions;
	private double expectedLifespan;
	private double thermalDesignPower;
	
	
	public NamedHost(String name, long ram, long bw, long storage, List<Pe> peList, double embodiedEmissions, double expectedLifespan, double thermalDesignPower, String location) {
		super(ram, bw, storage, peList);
		this.name = name;
		this.embodiedEmissions = embodiedEmissions;
		this.expectedLifespan = expectedLifespan;
		this.thermalDesignPower = thermalDesignPower;
		this.gridCarbonIntensity = Utils.gcis.get(location) != null ? Utils.gcis.get(location) : DEFAULT_GRID_CARBON_INTENSITY;
		this.enableUtilizationStats();
	}
	
	public double getSci() {
		double timeInterval = this.getTotalExecutionTime();
		double usedPower = this.thermalDesignPower * calculateTeads(this.myUtil());
		double resourceShare = (double) this.getVmCreatedList().stream().mapToLong(obj -> obj.getPesNumber()).sum() / this.getPesNumber();
		double deviceEnergy = (timeInterval * usedPower) / 3600000; // This already considers the resource share
		double energy = deviceEnergy + NETWORK_ENERGY;
		double carbonOperational = this.gridCarbonIntensity * energy;
		double embodiedCarbon = this.embodiedEmissions * (timeInterval / this.expectedLifespan) * resourceShare;
		return carbonOperational + embodiedCarbon;
	}

	public Double myUtil() {
		return this.getVmCreatedList().stream().mapToDouble(obj -> obj.getPesNumber() * obj.getCpuUtilizationStats().getMean()).sum() / this.getPesNumber();
	}

	public String getName() {
		return name;
	}

	public double getEmbodiedEmissions() {
		return embodiedEmissions;
	}
	
	public double getExpectedLifespan() {
		return expectedLifespan;
	}
	
	public double getThermalDesignPower() {
		return thermalDesignPower;
	}
	
	public int getGridCarbonIntensity() {
		return gridCarbonIntensity;
	}

	public double getNetworkEnergy() {
		return NETWORK_ENERGY;
	}
	
	protected static double calculateTeads(double utilization) {
		for (int i = 0; i < UTIL_POINTS.length - 1; i++) {
            if (utilization >= UTIL_POINTS[i] && utilization <= UTIL_POINTS[i + 1]) {
                return TEADS_CURVE[i] + (utilization - UTIL_POINTS[i]) *
                        (TEADS_CURVE[i + 1] - TEADS_CURVE[i]) /
                        (UTIL_POINTS[i + 1] - UTIL_POINTS[i]);
            }
        }
        return utilization == 0 ? TEADS_CURVE[0] : TEADS_CURVE[TEADS_CURVE.length - 1];
	}
}

class LinkedVm extends VmSimple {
	private String hostName;
	
	public LinkedVm(double mipsCapacity, long pesNumber, String hostName) {
		super(mipsCapacity, pesNumber);
		this.hostName = hostName;
		this.enableUtilizationStats();
	}

	public String getHostName() {
		return hostName;
	}
}

class LinkedCloudlet extends CloudletSimple {
	private String name;
	private Vm relatedVm;
	private String username;
	private String interaction;
	
	public LinkedCloudlet(long length, int pesNumber, UtilizationModel utilizationModel, String name, Vm relatedVm, String username, String interaction) {
		super(length, pesNumber, utilizationModel);
		this.name = name;
		this.relatedVm = relatedVm;
		this.username = username;
		this.interaction = interaction;
	}
	
	public Vm getRelatedVm() {
		return relatedVm;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUsername() {
		return username;
	}

	public String getInteraction() {
		return interaction;
	}

	public double getSci() {
		double timeInterval = this.getTotalExecutionTime();
		if (!NamedHost.class.isInstance(this.getVm().getHost())) return 0.0;
		NamedHost currentHost = (NamedHost) this.getVm().getHost();
		double usedPower = currentHost.getThermalDesignPower() * NamedHost.calculateTeads(this.getUtilizationModelCpu().getUtilization());
		double resourceShare = (double) this.getPesNumber() / currentHost.getPesNumber();
		double deviceEnergy = resourceShare * ((timeInterval * usedPower) / 3600000); // This does not already consider the resource share
		double energy = deviceEnergy + currentHost.getNetworkEnergy();
		double carbonOperational = currentHost.getGridCarbonIntensity() * energy;
		double embodiedCarbon = currentHost.getEmbodiedEmissions() * (timeInterval / currentHost.getExpectedLifespan()) * resourceShare;
		return carbonOperational + embodiedCarbon;
	}
}

class CustomAllocationPolicy extends VmAllocationPolicyAbstract {
	private final List<NamedHost> allowedHosts;
	private final boolean hostsAreNamed;
	
	public CustomAllocationPolicy(List<NamedHost> allowedHosts) {
		this.allowedHosts = allowedHosts;
		this.hostsAreNamed = allowedHosts.stream().allMatch(host -> NamedHost.class.isInstance(host));
	}
	
	@Override
	protected Optional<Host> defaultFindHostForVm(Vm vm) {
		if(vm instanceof LinkedVm && hostsAreNamed) {
			return Optional.of(allowedHosts.stream()
					.filter(host -> host.isSuitableForVm(vm) && ((NamedHost) host).getName().equals(((LinkedVm) vm).getHostName()))
					.findFirst()
					.orElseThrow());
		} else {
			return Optional.of(allowedHosts.stream()
					.filter(host -> host.isSuitableForVm(vm))
					.findFirst()
					.orElseThrow());
		}
	}
}

class CustomTableBuilder extends TableBuilderAbstract<LinkedCloudlet> {
	
	private final List<? extends LinkedCloudlet> list;
	
	public CustomTableBuilder(List<? extends LinkedCloudlet> list, double t) {
		super(list, new MarkdownTable().setTitle("SIMULATION RESULTS (Run for " + String.format("%.2f" ,t) + " seconds)"));
		list.sort(Comparator.comparingDouble(LinkedCloudlet::getStartTime));
		this.list = list;
	}
	
	@Override
    protected void createTableColumns() {
		newColumn("User", "Name", LinkedCloudlet::getUsername);
		newColumn("Interaction", "Name", LinkedCloudlet::getInteraction);
		newColumn("Cloudlet", "Name", LinkedCloudlet::getName);
		newColumn("Status", "", obj -> obj.getStatus().name());
		newColumn("Datacenter", "Name", obj -> obj.getVm().getHost().getDatacenter().getName());
        newColumn("Host", "Name", cloudlet -> ((NamedHost) cloudlet.getVm().getHost()).getName());
        newColumn("Host PEs", "CPU cores", cloudlet -> cloudlet.getVm().getHost().getWorkingPesNumber());
        newColumn("VM", "Name", cloudlet -> cloudlet.getVm().getDescription());
        newColumn("VM PEs", "CPU cores", cloudlet -> cloudlet.getVm().getPesNumber());
        newColumn("CloudletLen", "MI", Cloudlet::getLength);
        newColumn("FinishedLen", "MI", Cloudlet::getFinishedLengthSoFar);
        newColumn("CloudletPEs", "CPU cores", Cloudlet::getPesNumber);
        newColumn("StartTime", "Seconds", cloudlet -> String.format("%.1f", cloudlet.getStartTime()));
        newColumn("FinishTime", "Seconds", cloudlet -> String.format("%.1f", cloudlet.getFinishTime()));
        newColumn("ExecTime", "Seconds", cloudlet -> String.format("%.1f", cloudlet.getTotalExecutionTime()));
		newColumn("SCI", "gCO2eq", cloudlet -> String.format("%.4f", cloudlet.getSci()));
    }
	
	private void newColumn(String title, String subtitle, Function<LinkedCloudlet, Object> f) {
		String fm = "%" + Math.max(this.list.stream().map(obj -> f.apply(obj).toString()).mapToInt(String::length).max().orElse(title.length()), Math.max(title.length(), subtitle.length())) + "s";
		addColumn(getTable().newColumn(String.format(fm, title), subtitle, fm), f);
	}
}

class VmTableBuilder extends TableBuilderAbstract<Vm> {
	
	private final List<? extends Vm> list;
	
	public VmTableBuilder(List<? extends Vm> list) {
		super(list, new MarkdownTable().setTitle("Hosts and VMs Statistics"));
		list.sort(Comparator.comparing(obj -> ((NamedHost) obj.getHost()).getName()));
		this.list = list;
	}
	
	@Override
    protected void createTableColumns() {
		newColumn("Host", "Name", obj -> ((NamedHost) obj.getHost()).getName());
		newColumn("Host SCI", "gCO2eq", obj -> String.format("%.4f", ((NamedHost) obj.getHost()).getSci()));
		newColumn("Host CPU Utilization", "Mean", obj -> String.format("%.2f%%", ((NamedHost) obj.getHost()).myUtil() * 100));
		newColumn("VM", "Name", Vm::getDescription);
		newColumn("VM CPU Utilization", "Mean", obj -> String.format("%.2f%%", obj.getCpuUtilizationStats().getMean() * 100));
    }
	
	private void newColumn(String title, String subtitle, Function<Vm, Object> f) {
		String fm = "%" + Math.max(this.list.stream().map(obj -> f.apply(obj).toString()).mapToInt(String::length).max().orElse(title.length()), Math.max(title.length(), subtitle.length())) + "s";
		addColumn(getTable().newColumn(String.format(fm, title), subtitle, fm), f);
	}
}

class User {
	private String name;
	private double startTime;
	
	public User(String name, double st) {
		this.name = name;
		this.startTime = st;
	}
	
	public double getStartTime() {
		return startTime;
	}
	
	public String getName() {
		return name;
	}
}

@FunctionalInterface
interface TriConsumer<T, U, V> {
	void accept(T t, U u, V v);
}

class ActivityNode {
	private TriConsumer<EventInfo, String, BiConsumer<EventInfo, String>> value;
	private List<ActivityNode> children = new ArrayList<>();
	private List<Integer> routingWeights = new ArrayList<>();
	private static int DEFAULT_WEIGHT = 100;
	
	public ActivityNode(TriConsumer<EventInfo, String, BiConsumer<EventInfo, String>> value) {
		this.value = value;
	}
	
	public void addChild(ActivityNode t){
		this.children.add(t);
		this.routingWeights.add(DEFAULT_WEIGHT);
	}
	
	public void addChild(ActivityNode t, int weight) {
		this.children.add(t);
		this.routingWeights.add(weight);
	}
	
	private ActivityNode getNext() {
		if(this.children.size() == 1) {
			return this.children.get(0);
		} else {
			int totalWeight = this.routingWeights.stream().mapToInt(Integer::intValue).sum();
			int extractedWeight = Utils.rng.nextInt(totalWeight + 1);
			int runningSum = 0;
			for(int j = 0; j < this.routingWeights.size(); j++) {
				runningSum += this.routingWeights.get(j);
				if(runningSum >= extractedWeight) return this.children.get(j);
			}
			return null;
		}
	}
	
	private BiConsumer<EventInfo, String> resolveValue() {
		if(children.isEmpty()) {
			return null;
		} else {
			ActivityNode next = getNext();
			return (evt, str) -> next.value.accept(evt, str, next.resolveValue());
		}
	}
	
	public void process(String username) {
		value.accept(null, username, resolveValue());
	}
	
	public void process(String username, EventInfo evt) {
		value.accept(evt, username, resolveValue());
	}
}

class SimulationResult {
	
	class InteractionResult {
		String interactionName;
		double avgSCI;
		double avgServiceTime;
		long throughput;
		
		@Override
		public String toString() {
			return "i:" + interactionName + ",SCI:" + avgSCI + ",ServiceTime:" + avgServiceTime + ",tp:" + throughput;
		}
	}
	
	class DeviceResult {
		String deviceName;
		double avgSCI;
		double avgServiceTime;
		long throughput;
		double avgUtilization;
		long droppedConnections;
		
		@Override
		public String toString() {
			return "d:" + deviceName + ",SCI:" + avgSCI + ",ServiceTime:" + avgServiceTime + ",tp:" + throughput + ",u:" + avgUtilization + ",drop:" + droppedConnections;
		}
	}
	
	List<InteractionResult> interactionResults = new ArrayList<>();
	List<DeviceResult> deviceResults = new ArrayList<>();
	
	public SimulationResult(List<LinkedCloudlet> cloudletList, List<NamedHost> hostRecap) {
		List<LinkedCloudlet> finishedCloudlets = cloudletList.stream().filter(c -> c.getStatus().equals(Cloudlet.Status.SUCCESS)).collect(Collectors.toList());
		for (NamedHost namedHost : hostRecap) {
			DeviceResult dr = new DeviceResult();
			dr.deviceName = namedHost.getName();
			dr.avgUtilization = namedHost.myUtil();
			dr.avgSCI = namedHost.getSci();
			dr.throughput = finishedCloudlets.stream().filter(c -> c.getVm().getHost().equals(namedHost)).count();
			dr.avgServiceTime = finishedCloudlets.stream().filter(c -> c.getVm().getHost().equals(namedHost)).mapToDouble(Cloudlet::getTotalExecutionTime).average().orElse(0.0);
			dr.droppedConnections = cloudletList.stream().filter(c -> c.getStatus().equals(Cloudlet.Status.QUEUED) && c.getVm().getHost().equals(namedHost)).map(LinkedCloudlet::getUsername).distinct().count();
			deviceResults.add(dr);
		}
		for (String interactionName : finishedCloudlets.stream().map(LinkedCloudlet::getInteraction).distinct().collect(Collectors.toList())) {
			InteractionResult ir = new InteractionResult();
			ir.interactionName = interactionName;
			ir.avgSCI = finishedCloudlets.stream().filter(c -> c.getInteraction().equals(interactionName)).mapToDouble(LinkedCloudlet::getSci).average().orElse(0.0);
			ir.avgServiceTime = finishedCloudlets.stream().filter(c -> c.getInteraction().equals(interactionName)).mapToDouble(Cloudlet::getTotalExecutionTime).average().orElse(0.0);
			ir.throughput = finishedCloudlets.stream().filter(c -> c.getInteraction().equals(interactionName)).map(LinkedCloudlet::getUsername).distinct().count();
			interactionResults.add(ir);
		}
	}
}

class Utils {
	static Random rng = new Random();
	static Map<String, Integer> gcis = new HashMap<>();
	static final int EXEC_TIME = 3600;
	static final double EXP_LAMBDA = 2;
	static final int REPETITIONS = 20;
	
	static {
		String currentRegion = null;
		String currentElement = null;
		
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory.createXMLStreamReader(Utils.class.getClassLoader().getResourceAsStream("gcis.xml"));
			
			while(reader.hasNext()) {
				int event = reader.next();
				
				switch(event) {
				case XMLStreamConstants.START_ELEMENT:
					currentElement = reader.getLocalName();
					if("region".equals(currentElement)) {
						currentRegion = reader.getAttributeValue(null, "name");
					}
					break;
					
				case XMLStreamConstants.CHARACTERS:
					String text = reader.getText().trim();
					if (!text.isEmpty() && "carbon_intensity".equals(currentElement)) {
						gcis.put(currentRegion.replaceAll("-", "_"), Integer.parseInt(text));
					}
					break;
					
				case XMLStreamConstants.END_ELEMENT:
					currentElement = null;
					break;
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
