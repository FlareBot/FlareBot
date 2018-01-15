package stream.flarebot.flarebot;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GitHandler {

    private static Git GIT;

    static {
        try {
            GIT = Git.open(new File("."));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Repository getRepository() {
        if (GIT != null) {
            return GIT.getRepository();
        }
        return null;
    }

    public static RevCommit getLatestCommit() {
        if (getRepository() != null) {
            Repository repo = getRepository();
            try (RevWalk revWalk = new RevWalk(repo)) {
                revWalk.sort(RevSort.COMMIT_TIME_DESC);
                Map<String, Ref> allRefs = repo.getRefDatabase().getRefs(RefDatabase.ALL);
                for (Ref ref : allRefs.values()) {
                    RevCommit commit = revWalk.parseCommit(ref.getLeaf().getObjectId());
                    revWalk.markStart(commit);
                }
                return revWalk.next();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static Git getGit() {
        return GIT;
    }

    public static String getLatestCommitId() {
        if (getLatestCommit() != null) {
            return getLatestCommit().abbreviate(8).name();
        }
        return null;
    }
}
