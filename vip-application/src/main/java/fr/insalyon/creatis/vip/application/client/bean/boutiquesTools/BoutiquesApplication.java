package fr.insalyon.creatis.vip.application.client.bean.boutiquesTools;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Representation of an application Boutiques descriptor
 *
 * @author Guillaume Vanel
 * @version %I%, %G%
 */
public class BoutiquesApplication implements IsSerializable {

    private String name;
    private String description;
    private String version;
    private Set<BoutiquesInput> inputs = new HashSet<>();
    // Input dependencies
    private Set<BoutiquesGroup> groups = new HashSet<>();
    // Other properties not used for launch form generation
    private String applicationLFN;
    private String author;
    private String commandLine;
    private String containerType;
    private String containerImage;
    private String containerIndex;
    private String schemaVersion;
    private String challengerEmail;
    private String challengerTeam;
    private Set<BoutiquesOutputFile> outputFiles = new HashSet<>();
    private Map<String, String> tags = new HashMap<>();
    private String jsonFile;

    private BoutiquesApplicationExtensions boutiquesExtensions;

    public BoutiquesApplication() {
    }

    /**
     * @param name        String
     * @param description String
     * @param version     String
     */
    public BoutiquesApplication(String name, String description, String version) {
        this.name = name;
        this.description = description;
        this.version = version;
    }

    public BoutiquesApplicationExtensions getBoutiquesExtensions() {
        return boutiquesExtensions;
    }

    public void setBoutiquesExtensions(BoutiquesApplicationExtensions boutiquesExtensions) {
        if (this.boutiquesExtensions != null) {
            throw new IllegalStateException("A boutiques application can only be extended once");
        }
        this.boutiquesExtensions = boutiquesExtensions;
    }

    /**
     * @return String of format 'applicationName applicationVersion'
     */
    public String getFullName() {
        return this.name + " " + this.version;
    }

    /**
     * @return Application description as String
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return Array of BoutiquesInputs representing application inputs
     */
    public Set<BoutiquesInput> getInputs() {
        return this.inputs;
    }

    public Optional<BoutiquesInput> getInput(String inputId) {
        return this.inputs.stream().filter(input -> input.getId().equals(inputId)).findAny();
    }

    /**
     * @return Array of BoutiquesGroups representing application input groups
     */
    public Set<BoutiquesGroup> getGroups() {
        return groups;
    }

    /**
     * Generic method for getters of input attribute maps.
     *
     * @param inputGetter Function: getter implemented in BoutiquesInput to return an input attribute.
     * @param <T>         Type of the input attribute
     * @return Map from String input IDs to the attribute of those inputs
     */
    private <T> Map<String, T> getMap(Function<BoutiquesInput, T> inputGetter) {
        return this.inputs.stream()
                .filter(input -> inputGetter.apply(input) != null)
                .collect(Collectors.toMap(BoutiquesInput::getId, inputGetter));
    }

    /**
     * @return Map representing input dependencies of type 'disables-inputs'. Keys are disabling input IDs as Strings,
     * and values are arrays of Strings representing IDs of inputs disabled when disabling input is non empty
     */
    public Map<String, Set<String>> getDisablesInputsMap() {
        return this.getMap(BoutiquesInput::getDisablesInputsId);
    }

    /**
     * @return Map representing input dependencies of type 'requires-inputs'. Keys are dependant input IDs as Strings,
     * and values are arrays of Strings representing IDs of inputs that need to be non-empty for dependant input to be
     * enabled
     */
    public Map<String, Set<String>> getRequiresInputsMap() {
        return this.getMap(BoutiquesInput::getRequiresInputsId);
    }

    /**
     * @return Map representing input dependencies of type 'value-disables'. Keys are disabling input IDs as Strings,
     * and values are Maps of String values to arrays of Strings representing IDs of inputs disabled when
     * corresponding value of disabling input is selected
     */
    public Map<String, Map<String, Set<String>>> getValueDisablesInputsMap() {
        return this.getMap(BoutiquesInput::getValueDisablesInputsId);
    }

    /**
     * @return Map representing input dependencies of type 'value-requires'. Keys are dependant input IDs as Strings,
     * and values are Maps of String values to arrays of Strings representing IDs of inputs that need to be non empty
     * when corresponding value of dependant input is selected
     */
    public Map<String, Map<String, Set<String>>> getValueRequiresInputsMap() {
        return this.getMap(BoutiquesInput::getValueRequiresInputsId);
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getToolVersion() {
        return version;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    public String getContainerIndex() {
        return containerIndex;
    }

    public void setContainerIndex(String containerIndex) {
        this.containerIndex = containerIndex;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getChallengerEmail() {
        return challengerEmail;
    }

    public void setChallengerEmail(String challengerEmail) {
        this.challengerEmail = challengerEmail;
    }

    public String getChallengerTeam() {
        return challengerTeam;
    }

    public void setChallengerTeam(String challengerTeam) {
        this.challengerTeam = challengerTeam;
    }

    public Set<BoutiquesOutputFile> getOutputFiles() {
        return outputFiles;
    }

    public String getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(String jsonFile) {
        this.jsonFile = jsonFile;
    }

    public String getApplicationLFN() {
        return applicationLFN;
    }

    public void setApplicationLFN(String applicationLFN) {
        this.applicationLFN = applicationLFN;
    }

    public String getWrapperLFN() {
        return this.applicationLFN + "/bin/" + getName() + ".sh";
    }

    public String getWrapperName() {
        return getName() + ".sh";
    }

    public String getGASWLFN() {
        return this.applicationLFN + "/gasw/" + getName() + ".xml";
    }

    public String getGwendiaLFN() {
        return this.applicationLFN + "/workflow/" + getName() + ".gwendia";
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getJsonLFN() {
        return this.applicationLFN + "/json/" + getName() + ".json";
    }

    public void addInput(BoutiquesInput input) {
        this.inputs.add(input);
    }

    public void addGroup(BoutiquesGroup group) {
        this.groups.add(group);
    }

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

}