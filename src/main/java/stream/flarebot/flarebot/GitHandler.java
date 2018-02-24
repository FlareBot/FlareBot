package stream.flarebot.flarebot;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

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

    private static RevCommit getLatestCommit() {
        if (latestCommit == null) {
            if (getRepository() != null) {
                try (RevWalk walk = new RevWalk(getRepository())) {
                    Ref head = getRepository().exactRef(getRepository().getFullBranch());
                    GitHandler.latestCommit = walk.parseCommit(head.getObjectId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
