package de.codingair.codingapi.tools;

public abstract class Callback<E> {
    public abstract void accept(E object);

    public static class Result<T> {
		private T value;

        public Result(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}