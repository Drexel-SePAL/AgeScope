package StaticUIAnalyzer.Base;

import StaticUIAnalyzer.Model.ResultReport;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SootBase {
    public static List<String> excludePackagesList = new ArrayList<>();
    public String apkPath;
    public String platformPath;
    public ResultReport result;

    public SootBase(String apkPath, String platformPath, ResultReport result) {
        this.apkPath = apkPath;
        this.platformPath = platformPath;
        this.result = result;
    }

    public SootBase() {
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public void setPlatformPath(String platformPath) {
        this.platformPath = platformPath;
    }


    public void addExcludeClass() {
        excludePackagesList.add("java.");
        excludePackagesList.add("android.");
        excludePackagesList.add("androidx.");
        excludePackagesList.add("com.android.");
        excludePackagesList.add("javax.");
        excludePackagesList.add("android.support.");
        excludePackagesList.add("sun.");
        excludePackagesList.add("com.google.");
    }

    public void initSootConfig() {
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_jimple);
        String androidJarPath = Scene.v().getAndroidJarPath(platformPath, apkPath);

        List<String> pathList = new ArrayList<>();
        pathList.add(apkPath);
        pathList.add(androidJarPath);

        Options.v().set_process_dir(pathList);
        Options.v().set_force_android_jar(androidJarPath);
        Options.v().set_keep_line_number(true);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_allow_phantom_refs(true);
//        Options.v().set_whole_program(true);

        Options.v().set_wrong_staticness(Options.wrong_staticness_ignore);

        addExcludeClass();
        Options.v().set_exclude(excludePackagesList);

        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();
    }

    public boolean possibleVerificationClass(SootClass a) {
        var editTextCount = 0;
        for (var f : a.getFields()) {
            if (f.getSubSignature().contains("android.widget.EditText")) {
                editTextCount++;
            }
        }

        return editTextCount >= 2;
    }


    public boolean isExtendedFrom(SootClass sootClass, String className) {
        if (sootClass.getName().equals(className)) {
            return true;
        }

        if (sootClass.hasSuperclass()) {
            return isExtendedFrom(sootClass.getSuperclass(), className);
        }

        return false;
    }

    public HashSet<SootClass> findClassesByName(String className, boolean all) {
        var activitiesClass = new HashSet<SootClass>();
        var apkClasses = all ? Scene.v().getClasses() : Scene.v().getClasses().stream().filter(s -> !(s.getName().startsWith("java.")) &&
                !(s.getName().startsWith("com.android.")) && !(s.getName().startsWith("javax.")) &&
                !(s.getName().startsWith("android.support.")) && !(s.getName().startsWith("sun.")) &&
//              !(s.getName().startsWith("android.")) && !(s.getName().startsWith("androidx.")) &&
                !(s.getName().startsWith("com.google."))).toList();

        for (var cls : apkClasses) {
            if (activitiesClass.contains(cls)) {
                continue;
            }

            if (isExtendedFrom(cls, className)) {
                activitiesClass.add(cls);
            }
        }

        return activitiesClass;
    }
}
