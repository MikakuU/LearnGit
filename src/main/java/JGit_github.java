import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.util.FS;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class JGit_github {
    private String accessToken = "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";
    private String remotePath = "https://gitlab-ci-token:" + accessToken + "@github.com/MikakuU/LearnGit.git";
    private String localPath = "/Users/admin/localPath";

    public static void main(String[] args) {
    }

    //localRepoPath 为本地文件夹
    //keyPath 私钥文件 path
    //remoteRepoPath 为 ssh git远端仓库地址

    @Test
    public void gitClone() {
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(remotePath)
                .setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", accessToken)
                );
        Git git = null;
        try {
            git = cloneCommand
                    .setDirectory(new File(localPath))
                    .call();
            System.out.println("Clone success");
        } catch (Exception e) {
            System.out.println("Clone fail");
            e.printStackTrace();
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

    @Test
    public void pullCode() {
        try {
            //关联到本地仓库
            FileRepository fileRepository = new FileRepository(new File(localPath));
            Git pullGit = new Git(fileRepository);
            //设置密钥,拉取文件
            PullCommand pullCommand = pullGit.pull();
            pullCommand.call();
            System.out.println("Pull success");
        } catch (Exception e) {
            System.out.println("Pull fail");
            e.printStackTrace();
        }
    }


    //此方法获取了仓库内(path下,有可能为仓库下子文件夹)的所有提交版本号
    public static List<String> getGitVersions(String path) {
        List<String> versions = new ArrayList<>();
        try {
            Git git = Git.open(new File(path));
            Repository repository = git.getRepository();
            Git git1 = new Git(repository);
            Iterable<RevCommit> commits = git.log().all().call();
            int count = 0;
            for (RevCommit commit : commits) {
                System.out.println("LogCommit: " + commit);
                System.out.println("===" + commit.getFullMessage());
                String version = commit.getName(); //版本号,用来查询详细信息
                versions.add(version);
                System.out.println("===" + commit.getName());
                System.out.println("===" + commit.getAuthorIdent());
                count++;
            }
            return versions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取两个版本间提交详细记录
     *
     * @param localPath
     * @param oldVersion 上一个方法查询出来的版本号
     * @param newVersion 上一个方法查询出来的版本号
     */
    public static void showDiff(String localPath, String oldVersion, String newVersion) {
        try {
            Git git = Git.open(new File(localPath));
            Repository repository = git.getRepository();
            //旧版本
            AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldVersion);
            //新版本
            AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newVersion);

            List<DiffEntry> diff = git.diff().
                    setOldTree(oldTreeParser).
                    setNewTree(newTreeParser).
                    call();
            for (DiffEntry entry : diff) {
                System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                //此处可传一个输出流获取提交详情
                DiffFormatter formatter = new DiffFormatter(System.out);
                formatter.setRepository(repository);
                formatter.format(entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
