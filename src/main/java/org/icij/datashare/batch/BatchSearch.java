package org.icij.datashare.batch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.icij.datashare.text.ProjectProxy;
import org.icij.datashare.time.DatashareTime;
import org.icij.datashare.user.User;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class BatchSearch extends BatchSearchRecord {

    public final LinkedHashMap<String, Integer> queries; // LinkedHashMap keeps insert order
    public final List<String> fileTypes;
    public final List<String> paths;
    public final int fuzziness;
    public final boolean phraseMatches;

    public final String queryBody;
    // batch search creation

    public BatchSearch(final List<ProjectProxy> projects, final String name, final String description, User user) {
        this(UUID.randomUUID().toString(), projects, name, description, new LinkedHashMap<>(), DatashareTime.getInstance().now(), State.QUEUED, user,
                0, false, null, null, null,0,false, null, null);
    }
    public BatchSearch(final List<ProjectProxy> projects, final String name, final String description, final LinkedHashSet<String> queries, User user) {
        this(UUID.randomUUID().toString(), projects, name, description, toLinkedHashMap(queries), DatashareTime.getInstance().now(), State.QUEUED, user,
                0, false, null, null, null, 0,false, null, null);
    }
    public BatchSearch(final List<ProjectProxy> projects, final String name, final String description, final LinkedHashSet<String> queries, User user, boolean published) {
        this(UUID.randomUUID().toString(), projects, name, description, toLinkedHashMap(queries), DatashareTime.getInstance().now(), State.QUEUED, user, 0, published, null, null,null, 0,false, null, null);
    }
    public BatchSearch(final List<ProjectProxy> projects, final String name, final String description, final LinkedHashSet<String> queries, User user, boolean published, List<String> fileTypes, String queryBody, List<String> paths, int fuzziness) {
        this(UUID.randomUUID().toString(), projects, name, description, toLinkedHashMap(queries), DatashareTime.getInstance().now(), State.QUEUED, user, 0, published, fileTypes, queryBody, paths, fuzziness,false, null, null);
    }

    public BatchSearch(final List<ProjectProxy> projects, final String name, final String description, final LinkedHashSet<String> queries, User user, boolean published, List<String> fileTypes, String queryBody,List<String> paths, int fuzziness,boolean phraseMatches) {
        this(UUID.randomUUID().toString(), projects, name, description, toLinkedHashMap(queries), DatashareTime.getInstance().now(), State.QUEUED, user, 0, published, fileTypes, queryBody, paths, fuzziness,phraseMatches, null, null);
    }

    public BatchSearch(final List<ProjectProxy> projects, final String name, final String description, final LinkedHashSet<String> queries, User user, boolean published, List<String> fileTypes, String queryBody, List<String> paths,boolean phraseMatches) {
        this(UUID.randomUUID().toString(), projects, name, description, toLinkedHashMap(queries), DatashareTime.getInstance().now(), State.QUEUED, user, 0, published, fileTypes, queryBody, paths, 0,phraseMatches, null, null);
    }

    // copy constructor
    public BatchSearch(final BatchSearch toCopy) {this(toCopy, new HashMap<>());}
    public BatchSearch(final BatchSearch toCopy, final Map<String, String> overriddenParameters) {
        this(UUID.randomUUID().toString(), toCopy.projects,
                ofNullable(overriddenParameters.get("name")).orElse(toCopy.name),
                ofNullable(overriddenParameters.get("description")).orElse(toCopy.description), toCopy.queries, DatashareTime.getNow(), State.QUEUED,
                toCopy.user, 0, toCopy.published, toCopy.fileTypes, toCopy.queryBody,toCopy.paths, toCopy.fuzziness, toCopy.phraseMatches, null, null);
    }

    public BatchSearch(String uuid, List<ProjectProxy> projects, String name, String description, LinkedHashSet<String> queries, Date date, State state, User user) {
        this(uuid, projects, name, description, toLinkedHashMap(queries), date, state, user,
                0, false, null, null, null, 0,false, null, null);
    }

    // for tests
    public BatchSearch(final List<ProjectProxy> projects, final String name, final String description, final LinkedHashSet<String> queries, final Date date, final State state, final boolean published) {
        this(UUID.randomUUID().toString(), projects, name, description, toLinkedHashMap(queries), date, state, User.local(),
                0, published, null, null, null, 0,false, null, null);
    }

    public BatchSearch(final List<ProjectProxy> projects, final String name, final String description, final LinkedHashSet<String> queries, final Date date) {
        this(projects, name, description, queries, date, State.QUEUED, false);
    }

    // retrieved from persistence
    public BatchSearch(String uuid, List<ProjectProxy> projects, String name, String description, LinkedHashMap<String, Integer> queries, Date date, State state, User user,
                       int nbResults, boolean published, List<String> fileTypes, String queryBody, List<String> paths, int fuzziness, boolean phraseMatches, String errorMessage, String errorQuery) {
        super(uuid,projects,name,description,queries.size(),date,state,user,nbResults,published,errorMessage, errorQuery);
        if (this.nbQueries == 0) throw new IllegalArgumentException("queries cannot be empty");
        this.queries = queries;
        this.fileTypes = unmodifiableList(ofNullable(fileTypes).orElse(new ArrayList<>()));
        this.queryBody = queryBody;
        this.paths = unmodifiableList(ofNullable(paths).orElse(new ArrayList<>()));
        this.fuzziness = fuzziness;
        this.phraseMatches=phraseMatches;
    }

    /**
     * Allow creation of batch search without queries
     */
    public BatchSearch(String uuid, List<ProjectProxy> projects, String name, String description, Integer nbQueries, Date date, State state, User user,
                       int nbResults, boolean published, List<String> fileTypes, String queryBody, List<String> paths, int fuzziness, boolean phraseMatches,
                       String errorMessage, String errorQuery) {
        super(uuid,projects,name,description,nbQueries,date,state,user,nbResults,published,errorMessage, errorQuery);
        this.queries = new LinkedHashMap<>();
        this.fileTypes = unmodifiableList(ofNullable(fileTypes).orElse(new ArrayList<>()));
        this.queryBody = queryBody;
        this.paths = unmodifiableList(ofNullable(paths).orElse(new ArrayList<>()));
        this.fuzziness = fuzziness;
        this.phraseMatches=phraseMatches;
    }

    public boolean hasQueryBody() {
        return queryBody != null;
    }

    @JsonIgnore
    public List<String> getQueryList() {return new ArrayList<>(queries.keySet());}

    private static LinkedHashMap<String, Integer> toLinkedHashMap(LinkedHashSet<String> queries) {
        return ofNullable(queries).orElseThrow(() -> new IllegalArgumentException("queries cannot be null")).stream().collect(toMap(identity(), i -> 0,
                (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                LinkedHashMap::new));
    }

    @Override
    public String toString() {
        return "BatchSearch{" + uuid + " name='" + name + '\'' + " (" + state + ")}";
    }
}
