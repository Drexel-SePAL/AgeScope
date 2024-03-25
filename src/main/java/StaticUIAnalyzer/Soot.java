//package StaticAnalyzer;
//
//import soot.*;
//import soot.jimple.toolkits.callgraph.CallGraph;
//import soot.jimple.toolkits.callgraph.Edge;
//import soot.options.Options;
//
//import java.util.Collections;
//import java.util.Iterator;
//
//public class Main {
//    public static void main(String[] args) {
//        var apkPath = "/Users/fanfannnmn/Downloads/5C236D204382E359236EC551490268E80838962B99B8B827FE057FF599AA065D.apk";
//        var platforms = "/opt/homebrew/share/android-commandlinetools/platforms";
//        var androidJar = platforms + "/android-28/android.jar";
//
//        Options.v().set_src_prec(Options.src_prec_apk);
//        Options.v().set_android_jars(platforms);
//        Options.v().set_process_dir(Collections.singletonList(apkPath));
//        Options.v().set_force_android_jar(androidJar);
//        Options.v().set_allow_phantom_refs(true);
//        Options.v().set_output_format(Options.output_format_jimple);
//        Options.v().set_whole_program(true);
//
//        // Initialize Soot with these options
//        Scene.v().loadNecessaryClasses();
//
//        // Generate the Call Graph
//        PackManager.v().runPacks();
//
//        // Load the APK and resolve classes
//        Scene.v().loadNecessaryClasses();
//
//        // Get all layout classes
//        Iterator<SootClass> layoutClasses = Scene.v().getClasses().stream()
//                .filter(c -> c.getName().startsWith("activity."))
//                .iterator();
//
//        // Print layout classes
//        System.out.println("Layout files:");
//        while (layoutClasses.hasNext()) {
//            SootClass layoutClass = layoutClasses.next();
//            System.out.println(layoutClass.getName().substring("layout.".length()));
//        }
//
//    }
//}