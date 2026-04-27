public class Main {
    public static void main(String[] args) {
        //создаю объекты животных и ветеринара
        Dog d = new Dog();
        Cat c = new Cat();
        Horse h = new Horse();
        Vet v = new Vet();

        // ветеринар лечит каждое животное (полиморфизм в действии)
        v.treatAnimal(d);
        v.treatAnimal(c);
        v.treatAnimal(h);
    }
}
/**
 * Я улучшила класс Animal — добавила модификаторы доступа,
 * чтобы скрыть внутренние поля и защитить их от прямого изменения.
 * Раньше поля были package-private (без явного модификатора) — это плохо для инкапсуляции.
 */
class Animal {
    // Также делала поля private — теперь доступ только через геттеры
    private final String food;     // добавила final, потому что еда животного не меняется после создания
    private final String location; // локация тоже неизменна для конкретного животного
    private final String type;     // тип (вид) животного — тоже константа

    // конструктор теперь принимает все параметры, но они не могут быть null
    public Animal(String food, String location, String type) {
        // добавила простую валидацию — если параметр null, заменяю на "неизвестно"
        // (это улучшает надежность, чтобы программа не упала с NPE)
        this.food = food != null ? food : "неизвестно";
        this.location = location != null ? location : "неизвестно";
        this.type = type != null ? type : "неизвестно";
    }/**
     * Я изменила сигнатуру метода makeNoise() — добавила параметр,
     * чтобы звук зависел от контекста (например, днем и ночью может быть по-разному).
     * Раньше метод был без параметров, теперь он более гибкий.
     * В задании не было запрета менять сигнатуру.
     */
    public void makeNoise(String timeOfDay) {
        System.out.println("издает звук в " + timeOfDay);
    }
    // Я добавила перегруженную версию для обратной совместимости с существующим кодом
    // (чтобы классы Dog, Cat, Horse не ломались, если они вызывают makeNoise без параметров)
    public void makeNoise() {
        System.out.println("издает звук");
    }

    public void eat() {
        // улучшила вывод — теперь понятно, кто именно ест (использую getType())
        System.out.println(getType() + " ест " + this.food);
    }

    public void sleep() {
        //аналогично добавила тип животного в вывод
        System.out.println(getType() + " спит");
    }

    //югеттеры оставила без изменений, но они теперь возвращают final поля
    public String getLocation() {
        return location;
    }

    public String getFood() {
        return food;
    }

    public String getType() {
        return type;
    }
}/**
 * Еще улучшила класс Dog — добавила явный вызов super() для наглядности,
 * хотя компилятор вызывает его автоматически. Также добавила переопределение eat() и sleep().
 */
class Dog extends Animal {
    public Dog() {
        //явно вызываю конструктор родителя — это хороший тон, показывает намерение
        super("кость", "будка", "собака");
    }

    @Override
    public void makeNoise() {
        System.out.println("собака лает");
    }
    // добавила переопределение eat(), чтобы показать специфику собаки
    @Override
    public void eat() {
        System.out.println("собака с аппетитом грызет " + getFood());
    }

    //добавила переопределение sleep()
    @Override
    public void sleep() {
        System.out.println("собака свернулась калачиком и спит в " + getLocation());
    }
}
class Cat extends Animal {
    public Cat() {
        super("молоко", "дом", "кошка");
    }

    @Override
    public void makeNoise() {
        System.out.println("кошка мяукает");
    }

    //кошка ест иначе, чем обычное животное — переопределяю
    @Override
    public void eat() {
        System.out.println("кошка грациозно лакает " + getFood());
    }

    @Override
    public void sleep() {
        System.out.println("кошка спит на подоконнике в " + getLocation());
    }
}

class Horse extends Animal {
    public Horse() {
        super("яблоко", "конюшня", "лошадь");
    }

    @Override
    public void makeNoise() {
        System.out.println("лошадь ржет");
    }

    //добавила переопределение для единообразия с другими классами
    @Override
    public void eat() {
        System.out.println("лошадь жует " + getFood());
    }

    @Override
    public void sleep() {
        System.out.println("лошадь спит стоя в " + getLocation());
    }
}/**
 *  улучшила класс Vet — раньше он просто выводил информацию,
 * но метод treatAnimal имел побочный эффект (вывод в консоль) и не возвращал ничего.
 * Я добавила возможность получать строку с результатом лечения, не нарушая старую логику.
 * Это улучшение без оптимизации — просто расширение функциональности.
 */
class Vet {
    //юоставила старый метод для совместимости, но делегирую его новому методу
    public void treatAnimal(Animal animal) {
        // Студентка: вызываю новый метод, который возвращает строку, и вывожу её
        System.out.println(getTreatmentDescription(animal));
    }
    /**
     *новый метод — возвращает описание лечения как строку.
     * Это улучшает тестируемость и переиспользование кода.
     */
    public String getTreatmentDescription(Animal animal) {
        //добавила проверку на null — если вдруг передадут null, не упадет
        if (animal == null) {
            return "Ветеринар лечит неизвестное животное (null)";
        }
        return "Ветеринар лечит " + animal.getType() +
                ", который пришел из: " + animal.getLocation() +
                " и ест " + animal.getFood();
    }
}