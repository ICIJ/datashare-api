package org.icij.datashare.batch;

import org.icij.datashare.text.Project;
import org.icij.datashare.user.User;

import java.util.*;

public class BatchSearchSummary {

    public enum State {QUEUED, RUNNING, SUCCESS, FAILURE}
    public final String uuid;
    public final boolean published;
    public final Project project;
    public final String name;
    public final String description;
    public final User user;
    public final State state;
    public final Date date;
    private final int nbQueries;
    public final int nbResults;

    // for tests
    public BatchSearchSummary(final Project project, final String name, final String description, final int nbQueries, Date date) {
        this(UUID.randomUUID().toString(), project, name, description, nbQueries, date, State.QUEUED, User.local(),
                0, false);
    }

    public BatchSearchSummary(String uuid, Project project, String name, String description, int nbQueries, Date date, State state, User user,
                              int nbResults, boolean published){
        assert date != null && uuid != null;
        this.uuid = uuid;
        this.published = published;
        this.project = project;
        this.name = name;
        this.description = description;
        this.user = user;
        this.date = date;
        this.nbResults = nbResults;
        this.nbQueries = nbQueries;
        this.state = state;

    }

    public int getNbQueries(){
        return nbQueries;
    }

    @Override
    public String toString() {
        return "BatchSearchSummary{" + uuid + " name='" + name + '\'' + " (" + state + ")}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchSearchSummary that = (BatchSearchSummary) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}