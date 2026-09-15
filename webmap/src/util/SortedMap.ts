export class SortedMap<K, V> extends Map<K, V> {
    private readonly compare: (a: readonly [K, V], b: readonly [K, V]) => number;

    constructor(
        entries?: readonly (readonly [K, V])[] | null,
        compareFn?: (a: readonly [K, V], b: readonly [K, V]) => number
    ) {
        super();

        this.compare = compareFn || (([_k1, _v1]: readonly [K, V], [_k2, _v2]: readonly [K, V]) => (_k1 > _k2 ? 1 : _k1 < _k2 ? -1 : 0));

        if (entries) {
            for (const [key, value] of entries) {
                this.set(key, value);
            }
        }
    }

    override set(key: K, value: V): this {
        super.set(key, value);

        const sorted: [K, V][] = [...super.entries()].sort(this.compare);

        super.clear();

        for (const [key, value] of sorted) {
            super.set(key, value);
        }

        return this;
    }
}
