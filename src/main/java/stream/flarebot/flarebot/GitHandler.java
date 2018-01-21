package stream.flarebot.flarebot;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
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

    private static final Git GIT;
    private static RevCommit latestCommit;

    static {
        Git git;
        try {
            git = Git.open(new File("FlareBot/"));
        } catch (RepositoryNotFoundException e) {
            try {
                git =
                        Git.cloneRepository().setDirectory(new File("FlareBot/")).setURI("https://github.com/FlareBot/FlareBot.git").call();
            } catch (GitAPIException e1) {
                e1.printStackTrace();
                git = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            git = null;
        }
        GIT = git;
    }

    public static Repository getRepository() {
        if (GIT != null) {
            return GIT.getRepository();
        }
        return null;
    }

    public static void updateRepo(File directory) throws GitAPIException {
        if (GIT != null) {
            GIT.pull().call();
        } else {
            Git.cloneRepository().setDirectory(directory).setURI("https://github.com/FlareBot/FlareBot.git").call();
        }

    }

    public static RevCommit getLatestCommit() {
        if (latestCommit == null) {
            RevCommit latestCommit = null;
            if (getRepository() != null) {
                Repository repo = getRepository();
                try (RevWalk revWalk = new RevWalk(repo)) {
                    revWalk.sort(RevSort.COMMIT_TIME_DESC);
                    Map<String, Ref> allRefs = repo.getRefDatabase().getRefs(RefDatabase.ALL);
                    for (Ref ref : allRefs.values()) {
                        RevCommit commit = revWalk.parseCommit(ref.getLeaf().getObjectId());
                        revWalk.markStart(commit);
                    }
                    latestCommit = revWalk.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            GitHandler.latestCommit = latestCommit;
        }
        return latestCommit;
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
