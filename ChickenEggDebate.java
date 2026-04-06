import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;

/**
 * Класс ChickenEggDebate
 * Я, как студентка, добавила сюда современные инструменты отладки:
 * 1. ThreadMXBean - для глубокого анализа состояния потоков
 * 2. Мониторинг блокировок и ожиданий потоков
 * 3. Профайлер задержек для измерения точности sleep()
 */


public class ChickenEggDebate {

    public static void main(String[] args) {
        System.out.println("СПОР: ЧТО ПОЯВИЛОСЬ РАНЬШЕ?\n");

        // Создаю объекты для отладки (студентка добавила)
        AdvancedThreadMonitor monitor = new AdvancedThreadMonitor();
        LatencyProfiler profiler = new LatencyProfiler();

        // Создаю потоки, передаю в них профайлер
        Thread chickenThread = new Thread(new ChickenTask(profiler), "Курица");
        Thread eggThread = new Thread(new EggTask(profiler), "Яйцо");

        // Запускаю потоки
        chickenThread.start();
        eggThread.start();

        // Демонстрация метода isAlive()
        System.out.println("\nПРОВЕРКА СОСТОЯНИЯ ПОТОКОВ");
        System.out.println("Поток Курица активен: " + chickenThread.isAlive());
        System.out.println("Поток Яйцо активен: " + eggThread.isAlive());

        // Углубленный мониторинг (студентка добавила)
        monitor.monitorThread(chickenThread);
        monitor.monitorThread(eggThread);
        monitor.checkDeadlock();

        // Жду завершения потоков
        try {
            System.out.println("\nОЖИДАНИЕ ЗАВЕРШЕНИЯ ПОТОКОВ");

            chickenThread.join();
            System.out.println("Поток Курица завершил работу");

            eggThread.join();
            System.out.println("Поток Яйцо завершил работу");

        } catch (InterruptedException e) {
            System.out.println("Ожидание прервано!");
        }

        // Вывожу статистику по потокам
        monitor.printThreadStats();

        // Определяем победителя
        determineWinner();
    }

    /**
     * Класс для мониторинга потоков через ThreadMXBean
     *
     * ВАЖНО о ThreadMXBean:
     * - Это интерфейс из пакета java.lang.management
     * - Позволяет получать информацию о потоках без сторонних библиотек
     * - Метод findDeadlockedThreads() обнаруживает взаимные блокировки
     * - Работает на уровне JVM, минимальное влияние на производительность
     */
    static class AdvancedThreadMonitor {
        private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        // Метод для детального мониторинга потока
        public void monitorThread(Thread thread) {
            ThreadInfo info = threadMXBean.getThreadInfo(thread.getId());
            if (info != null) {
                System.out.println("\n[МОНИТОР] Поток: " + info.getThreadName());
                System.out.println("  - ID: " + info.getThreadId());
                System.out.println("  - Состояние: " + info.getThreadState());
                System.out.println("  - Заблокирован: " + (info.getLockName() != null ? info.getLockName() : "нет"));
                System.out.println("  - Кол-во блокировок: " + info.getBlockedCount());
                System.out.println("  - Кол-во ожиданий: " + info.getWaitedCount());
            }
        }

        // Проверка на deadlock (взаимную блокировку)
        public void checkDeadlock() {
            long[] deadlocked = threadMXBean.findDeadlockedThreads();
            if (deadlocked != null && deadlocked.length > 0) {
                System.out.println("\n[МОНИТОР] ВНИМАНИЕ! Обнаружен deadlock!");
                for (long id : deadlocked) {
                    ThreadInfo info = threadMXBean.getThreadInfo(id);
                    System.out.println("  - Поток '" + info.getThreadName() + "' заблокирован");
                }
            }
        }

        // Вывод статистики по всем потокам
        public void printThreadStats() {
            System.out.println("\n=== СТАТИСТИКА ПОТОКОВ ===");
            ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 0);
            for (ThreadInfo info : threadInfos) {
                if (info != null && (info.getThreadName().equals("Курица") || info.getThreadName().equals("Яйцо"))) {
                    System.out.println(info.getThreadName() + ":");
                    System.out.println("  - Блокировок: " + info.getBlockedCount());
                    System.out.println("  - Ожиданий: " + info.getWaitedCount());
                }
            }
        }
    }

    /**
     * Класс для измерения точности задержек
     *
     * ВАЖНО о профайлинге задержек:
     * - Thread.sleep() не гарантирует точное время из-за планировщика ОС
     * - Отклонение может составлять 10-50 мс и больше
     * - Нанотайминг (System.nanoTime()) дает точность до микросекунд
     * - Полезно для отладки race conditions и проблем с синхронизацией
     */
    static class LatencyProfiler {

        // Метод для измерения точности sleep()
        public void measureSleepAccuracy(Thread thread, long expectedMs) {
            long before = System.nanoTime();
            try {
                Thread.sleep(expectedMs);
            } catch (InterruptedException e) {
                thread.interrupt();
            }
            long after = System.nanoTime();
            long actualMs = (after - before) / 1_000_000;
            long deviation = Math.abs(actualMs - expectedMs);

            if (deviation > 20) {
                System.out.println("[ПРОФАЙЛЕР] " + thread.getName() +
                        ": ожидалось " + expectedMs + " мс, реально " + actualMs +
                        " мс (отклонение " + deviation + " мс)");
            }
        }
    }

    /**
     * Задача для потока "Курица"
     */
    static class ChickenTask implements Runnable {
        private final LatencyProfiler profiler;

        ChickenTask(LatencyProfiler profiler) {
            this.profiler = profiler;
        }

        public void run() {
            try {
                profiler.measureSleepAccuracy(Thread.currentThread(), 1500);
                System.out.println("\nКУРИЦА: Я появилась первой!");

                Thread.sleep(500);
                System.out.println("КУРИЦА: Без меня не было бы яиц!");

            } catch (InterruptedException e) {
                System.out.println("Курица прервана!");
            }
        }
    }

    /**
     * Задача для потока "Яйцо"
     */
    static class EggTask implements Runnable {
        private final LatencyProfiler profiler;

        EggTask(LatencyProfiler profiler) {
            this.profiler = profiler;
        }

        public void run() {
            try {
                profiler.measureSleepAccuracy(Thread.currentThread(), 1000);
                System.out.println("\nЯЙЦО: Я появилось первым!");

                Thread.sleep(1000);
                System.out.println("ЯЙЦО: Без меня не было бы кур!");

            } catch (InterruptedException e) {
                System.out.println("Яйцо прервано!");
            }
        }
    }

    /**
     * Метод для определения победителя спора
     */
    public static void determineWinner() {
        System.out.println("\nРЕЗУЛЬТАТ СПОРА");
        System.out.println("Последнее слово осталось за...");

        if (Math.random() > 0.5) {
            System.out.println("ПОБЕДИЛА КУРИЦА!");
            System.out.println("Вывод: Сначала появилась курица!");
        } else {
            System.out.println("ПОБЕДИЛО ЯЙЦО!");
            System.out.println("Вывод: Сначала появилось яйцо!");
        }
    }
}