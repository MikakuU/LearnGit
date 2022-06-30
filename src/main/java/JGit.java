import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.util.FS;
import org.junit.Test;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class JGit {

    public static void main(String[] args) {
        String localCodeDir = "/Users/admin/localPath"; //本地文件夹
        String remoteRepoPath = "git@git.corp.kuaishou.com:chenzhuwei/ksalbum.git"; //git地址
        String keyPath = "/Users/admin/.ssh/id_rsa";  //私钥文件
        gitClone(remoteRepoPath, localCodeDir, keyPath);
    }

    //localRepoPath 为本地文件夹
    //keyPath 私钥文件 path
    //remoteRepoPath 为 ssh git远端仓库地址

    @Test
    protected static void gitClone(String remoteRepoPath, String localRepoPath, String keyPath) {
        //ssh session的工厂,用来创建密匙连接
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch sch = super.createDefaultJSch(fs);
                sch.addIdentity(keyPath); //添加私钥文件
                return sch;
            }
        };

        //克隆代码库命令
        CloneCommand cloneCommand = Git.cloneRepository();
        Git git = null;
        try {
            git = cloneCommand.setURI(remoteRepoPath) //设置远程URI
                    .setTransportConfigCallback(transport -> {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(sshSessionFactory);
                    })
                    .setDirectory(new File(localRepoPath)) //设置下载存放路径
                    .call();
            System.out.println("success");
        } catch (Exception e) {
            System.out.println("fail");
            e.printStackTrace();
        } finally {
            if (git != null) {
                git.close();
            }
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
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
