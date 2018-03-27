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

    private static Git git;
    private static RevCommit latestCommit;

    public static Repository getRepository() {
        if (getGit() != null) {
            return getGit().getRepository();
        }
        return null;
    }

    public static void updateRepo(File directory) throws GitAPIException {
        if (getGit() != null) {
            getGit().pull().call();
        } else {
            Git.cloneRepository().setDirectory(directory).setURI("https://github.com/FlareBot/FlareBot.git").call();
        }

    }

    public static RevCommit getLatestCommit() {
        if (git != null && latestCommit == null) {
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
        try {
            git = Git.open(FlareBot.instance().isTestBot() ? new File(".") : new File("FlareBot/"));
        } catch (RepositoryNotFoundException e) {
            try {
                git = Git.cloneRepository()
                        .setDirectory(FlareBot.instance().isTestBot() ? new File(".") : new File("FlareBot/"))
                        .setURI("https://github.com/FlareBot/FlareBot.git")
                        .call();
            } catch (GitAPIException e1) {
                e1.printStackTrace();
                git = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            git = null;
        }
        return git;
    }

    public static String getLatestCommitId() {
        if (getLatestCommit() != null) {
            return getLatestCommit().abbreviate(8).name();
        }
        return null;
    }
}
