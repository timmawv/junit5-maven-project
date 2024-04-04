package avlyakulov.timur;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;

//сам класс нужен для того чтобы определить что и как запускать
public class TestLauncher {

    public static void main(String[] args) {
        Launcher launcher = LauncherFactory.create();

        SummaryGeneratingListener summaryGeneratingListener = new SummaryGeneratingListener();//нужен для того чтобы видить результаты тестов.

//        launcher.registerTestExecutionListeners();
//        launcher.registerTestExecutionListeners(summaryGeneratingListener);

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
//                .selectors(DiscoverySelectors.selectClass(UserServiceTest.class)) // это уже определяет где находяться наши классы
                .selectors(DiscoverySelectors.selectPackage("avlyakulov.timur.service")) // это уже определяет где находяться наши классы
//                .listeners()
                .filters(
                        TagFilter.includeTags("login")
                )
                .build();
        launcher.execute(request, summaryGeneratingListener);

        try (PrintWriter printWriter = new PrintWriter(System.out)) {
            summaryGeneratingListener.getSummary().printTo(printWriter);
        }
    }
}
