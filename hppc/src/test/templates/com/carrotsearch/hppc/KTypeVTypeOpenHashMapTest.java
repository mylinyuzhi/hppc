package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.TestUtils.*;

import java.util.*;

import org.assertj.core.api.Assertions;
import org.junit.*;

import com.carrotsearch.hppc.annotations.AwaitsFix;
import com.carrotsearch.hppc.cursors.*;
import com.carrotsearch.hppc.mutables.IntHolder;
import com.carrotsearch.hppc.predicates.*;
import com.carrotsearch.hppc.procedures.*;

/**
 * Tests for {@link KTypeVTypeOpenHashMap}.
 */
/* ! ${TemplateOptions.generatedAnnotation} ! */
public class KTypeVTypeOpenHashMapTest<KType, VType> extends AbstractKTypeTest<KType>
{
    protected VType value0 = vcast(0);
    protected VType value1 = vcast(1);
    protected VType value2 = vcast(2);
    protected VType value3 = vcast(3);

    /**
     * Per-test fresh initialized instance.
     */
    public KTypeVTypeOpenHashMap<KType, VType> map = KTypeVTypeOpenHashMap.newInstance();

    @After
    public void checkEmptySlotsUninitialized()
    {
        if (map != null)
        {
            int occupied = 0;
            for (int i = 0; i < map.keys.length; i++)
            {
                if (map.allocated[i] == false)
                {
                    /*! #if ($TemplateOptions.KTypeGeneric) !*/
                    assertEquals2(Intrinsics.defaultKTypeValue(), map.keys[i]);
                    /*! #end !*/
                    /*! #if ($TemplateOptions.VTypeGeneric) !*/
                    assertEquals2(Intrinsics.defaultVTypeValue(), map.values[i]);
                    /*! #end !*/
                }
                else
                {
                    occupied++;
                }
            }
            assertEquals(occupied, map.assigned);
        }
    }

    /**
     * Convert to target type from an integer used to test stuff. 
     */
    protected VType vcast(int value)
    {
        /*! #if ($TemplateOptions.VTypePrimitive)
            return (VType) value;
            #else !*/ 
            @SuppressWarnings("unchecked")        
            VType v = (VType)(Object) value;
            return v;
        /*! #end !*/
    }

    /**
     * Create a new array of a given type and copy the arguments to this array.
     */
    /* #if ($TemplateOptions.VTypeGeneric) */
    @SafeVarargs
    /* #end */
    protected final VType [] newvArray(VType... elements)
    {
        return elements;
    }

    private void assertSameMap(
        final KTypeVTypeMap<KType, VType> c1,
        final KTypeVTypeMap<KType, VType> c2)
    {
        assertEquals(c1.size(), c2.size());

        c1.forEach(new KTypeVTypeProcedure<KType, VType>()
        {
            public void apply(KType key, VType value)
            {
                assertTrue(c2.containsKey(key));
                assertEquals2(value, c2.get(key));
            }
        });
    }
    
    /* */
    @Test
    @Ignore
    public void testEnsureCapacity()
    {
        final IntHolder expands = new IntHolder();
        KTypeVTypeOpenHashMap<KType, VType> map = new KTypeVTypeOpenHashMap<KType, VType>(0) {
          /*! #if (false) NOCOMMIT: write after map refactoring.  #end !*/
        };

        // Add some elements.
        final int max = rarely() ? 0 : randomIntBetween(0, 250);
        for (int i = 0; i < max; i++) {
          map.put(cast(i), value0);
        }

        final int additions = randomIntBetween(max, max + 5000);
        map.ensureCapacity(additions + map.size());
        final int before = expands.value;
        for (int i = 0; i < additions; i++) {
          map.put(cast(i), value0);
        }
        assertEquals(before, expands.value);
    }
    
    /* */
    @Test
    public void testCloningConstructor()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        assertSameMap(map, KTypeVTypeOpenHashMap.from(map));
        assertSameMap(map, new KTypeVTypeOpenHashMap<KType, VType>(map));
    }

    /* */
    @Test
    public void testFromArrays()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        KTypeVTypeOpenHashMap<KType, VType> map2 = KTypeVTypeOpenHashMap.from(
            newArray(key1, key2, key3),
            newvArray(value1, value2, value3));

        assertSameMap(map, map2);
    }

    @Test
    public void testGetOrDefault()
    {
        map.put(key2, value2);
        assertTrue(map.containsKey(key2));

        map.put(key1, value1);
        assertEquals2(value1, map.getOrDefault(key1, value3));
        assertEquals2(value3, map.getOrDefault(key3, value3));
        map.remove(key1);
        assertEquals2(value3, map.getOrDefault(key1, value3));

        // Make sure lslot wasn't touched.
        assertEquals2(value2, map.lget());
    }

    /* */
    @Test
    public void testPut()
    {
        map.put(key1, value1);

        assertTrue(map.containsKey(key1));
        assertEquals2(value1, map.lget());
        assertEquals2(value1, map.get(key1));
    }

    /* */
    @Test
    public void testLPut()
    {
        map.put(key1, value2);
        if (map.containsKey(key1))
            map.lset(value3);

        assertTrue(map.containsKey(key1));
        assertEquals2(value3, map.lget());
        assertEquals2(value3, map.get(key1));
    }

    /* */
    @Test
    public void testPutOverExistingKey()
    {
        map.put(key1, value1);
        assertEquals2(value1, map.put(key1, value3));
        assertEquals2(value3, map.get(key1));
    }

    /* */
    @Test
    public void testPutWithExpansions()
    {
        final int COUNT = 10000;
        final Random rnd = new Random(randomLong());
        final HashSet<Object> values = new HashSet<Object>();

        for (int i = 0; i < COUNT; i++)
        {
            final int v = rnd.nextInt();
            final boolean hadKey = values.contains(cast(v));
            values.add(cast(v));

            assertEquals(hadKey, map.containsKey(cast(v)));
            map.put(cast(v), vcast(v));
            assertEquals(values.size(), map.size());
        }
        assertEquals(values.size(), map.size());
    }

    /* */
    @Test
    public void testPutAll()
    {
        map.put(key1, value1);
        map.put(key2, value1);

        KTypeVTypeOpenHashMap<KType, VType> map2 = 
            new KTypeVTypeOpenHashMap<KType, VType>();

        map2.put(key2, value2);
        map2.put(key3, value1);

        // One new key (key3).
        assertEquals(1, map.putAll(map2));
        
        // Assert the value under key2 has been replaced.
        assertEquals2(value2, map.get(key2));

        // And key3 has been added.
        assertEquals2(value1, map.get(key3));
        assertEquals(3, map.size());
    }
    
    /* */
    @Test
    public void testPutIfAbsent()
    {
        assertTrue(map.putIfAbsent(key1, value1));
        assertFalse(map.putIfAbsent(key1, value2));
        assertEquals2(value1, map.get(key1));
    }

    /*! #if ($TemplateOptions.VTypePrimitive)
    @Test
    public void testPutOrAdd()
    {
        assertEquals2(value1, map.putOrAdd(key1, value1, value2));
        assertEquals2(value1 + value2, map.putOrAdd(key1, value1, value2));
    }
    #end !*/

    /*! #if ($TemplateOptions.VTypePrimitive)
    @Test
    public void testAddTo()
    {
        assertEquals2(value1, map.addTo(key1, value1));
        assertEquals2(value1 + value2, map.addTo(key1, value2));
    }
    #end !*/

    /* */
    @Test
    public void testRemove()
    {
        map.put(key1, value1);
        assertEquals2(value1, map.remove(key1));
        assertEquals2(Intrinsics.defaultVTypeValue(), map.remove(key1));
        assertEquals(0, map.size());

        // These are internals, but perhaps worth asserting too.
        assertEquals(0, map.assigned);
    }

    /* */
    @Test
    public void testRemoveAllWithContainer()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);

        KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(newArray(key2, key3, key4));

        map.removeAll(list2);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(key1));
    }

    /* */
    @Test
    public void testRemoveAllWithPredicate()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);

        map.removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType value)
            {
                return value == key2 || value == key3;
            }
        });
        assertEquals(1, map.size());
        assertTrue(map.containsKey(key1));
    }

    /* */
    @Test
    public void testRemoveViaKeySetView()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);

        map.keys().removeAll(new KTypePredicate<KType>()
        {
            public boolean apply(KType value)
            {
                return value == key2 || value == key3;
            }
        });
        assertEquals(1, map.size());
        assertTrue(map.containsKey(key1));
    }

    /* */
    @Test
    public void testMapsIntersection()
    {
        KTypeVTypeOpenHashMap<KType, VType> map2 = 
            KTypeVTypeOpenHashMap.newInstance(); 

        map.put(key1, value1);
        map.put(key2, value1);
        map.put(key3, value1);
        
        map2.put(key2, value1);
        map2.put(key4, value1);

        assertEquals(2, map.keys().retainAll(map2.keys()));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(key2));
    }

    /* */
    @Test
    public void testMapKeySet()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);

        assertSortedListEquals(map.keys().toArray(), key1, key2, key3);
    }

    /* */
    @Test
    public void testMapKeySetIterator()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);

        int counted = 0;
        for (KTypeCursor<KType> c : map.keys())
        {
            assertEquals2(map.keys[c.index], c.value);
            counted++;
        }
        assertEquals(counted, map.size());
    }

    /* */
    @Test
    public void testClear()
    {
        map.put(key1, value1);
        map.put(key2, value1);
        map.clear();
        assertEquals(0, map.size());

        // These are internals, but perhaps worth asserting too.
        assertEquals(0, map.assigned);

        // Check if the map behaves properly upon subsequent use.
        testPutWithExpansions();
    }

    /* */
    @Test
    public void testIterable()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.remove(key2);

        int count = 0;
        for (KTypeVTypeCursor<KType, VType> cursor : map)
        {
            count++;
            assertTrue(map.containsKey(cursor.key));
            assertEquals2(cursor.value, map.get(cursor.key));

            assertEquals2(cursor.value, map.values[cursor.index]);
            assertEquals2(cursor.key, map.keys[cursor.index]);
            assertEquals2(true, map.allocated[cursor.index]);
        }
        assertEquals(count, map.size());

        map.clear();
        assertFalse(map.iterator().hasNext());
    }

    /* */
    @Test
    public void testFullLoadFactor()
    {
        final IntHolder reallocations = new IntHolder();
        final int elements = 0x7F;
        map = new KTypeVTypeOpenHashMap<KType, VType>(elements, 1d) {
          @Override
          protected double getLoadFactor() {
            // Skip load factor sanity range checking.
            return initialLoadFactor;
          }
          
          @Override
          protected void allocateBuffers(int arraySize, double loadFactor) {
            super.allocateBuffers(arraySize, loadFactor);
            reallocations.value++;
          }
        };

        // Fit in the byte key range.
        int reallocationsBefore = reallocations.value;
        assertEquals(reallocationsBefore, 1);
        for (int i = 0; i < elements; i++)
        {
            map.put(cast(i), value1);
        }

        // Still not expanded.
        assertEquals(reallocationsBefore, reallocations.value);
        // Must not expand (insertion of an existing key).
        map.put(cast(0), value2);
        assertEquals(reallocationsBefore, reallocations.value);
        // Expand now.
        map.put(cast(0xff), value2);
        assertEquals(reallocationsBefore + 1, reallocations.value);
    }

    /* */
    @Test
    public void testBug_HPPC73_FullCapacityGet()
    {
        final IntHolder reallocations = new IntHolder();
        final int elements = 0x7F;
        map = new KTypeVTypeOpenHashMap<KType, VType>(elements, 1d) {
          @Override
          protected double getLoadFactor() {
            // Skip load factor sanity range checking.
            return initialLoadFactor;
          }
          
          @Override
          protected void allocateBuffers(int arraySize, double loadFactor) {
            super.allocateBuffers(arraySize, loadFactor);
            reallocations.value++;
          }
        };

        int reallocationsBefore = reallocations.value;
        assertEquals(reallocationsBefore, 1);
        for (int i = 0; i < elements; i++)
        {
            map.put(cast(i), value1);
        }
        
        // Non-existent key.
        map.remove(cast(elements + 1));
        assertFalse(map.containsKey(cast(elements + 1)));
        assertEquals2(Intrinsics.defaultVTypeValue(), map.get(cast(elements + 1)));

        // Should not expand because we're replacing an existing element.
        map.put(cast(0), value2);
        assertEquals(reallocationsBefore, reallocations.value);

        map.putIfAbsent(cast(0), value3);
        assertEquals(reallocationsBefore, reallocations.value);

        // Remove from a full map.
        map.remove(cast(0));
        assertEquals(reallocationsBefore, reallocations.value);

        // Check expand on "last slot of a full map" condition.
        map.put(cast(0), value1);
        map.put(cast(elements), value1);
        assertEquals(reallocationsBefore + 1, reallocations.value);
    }

    @Test
    public void testHashCodeEquals()
    {
        KTypeVTypeOpenHashMap<KType, VType> l0 = 
            new KTypeVTypeOpenHashMap<KType, VType>();
        assertEquals(0, l0.hashCode());
        assertEquals(l0, new KTypeVTypeOpenHashMap<KType, VType>());

        KTypeVTypeOpenHashMap<KType, VType> l1 = KTypeVTypeOpenHashMap.from(
            newArray(key1, key2, key3),
            newvArray(value1, value2, value3));

        KTypeVTypeOpenHashMap<KType, VType> l2 = KTypeVTypeOpenHashMap.from(
            newArray(key2, key1, key3),
            newvArray(value2, value1, value3));

        KTypeVTypeOpenHashMap<KType, VType> l3 = KTypeVTypeOpenHashMap.from(
            newArray(key1, key2),
            newvArray(value2, value1));

        assertEquals(l1.hashCode(), l2.hashCode());
        assertEquals(l1, l2);

        assertFalse(l1.equals(l3));
        assertFalse(l2.equals(l3));
    }

    /* */
    @Test @AwaitsFix("HPPC-127") /*! #if (false) NOCOMMIT: HPPC-127 #end !*/ 
    public void testHashCodeEqualsDifferentPerturbance()
    {
        KTypeVTypeOpenHashMap<KType, VType> l0 = 
            new KTypeVTypeOpenHashMap<KType, VType>() {
            @Override
            protected int computePerturbationValue(int capacity)
            {
                return 0xDEADBEEF;
            }
        };

        KTypeVTypeOpenHashMap<KType, VType> l1 = 
            new KTypeVTypeOpenHashMap<KType, VType>() {
            @Override
            protected int computePerturbationValue(int capacity)
            {
                return 0xCAFEBABE;
            }
        };
        
        assertEquals(0, l0.hashCode());
        assertEquals(l0.hashCode(), l1.hashCode());
        assertEquals(l0, l1);

        KTypeVTypeOpenHashMap<KType, VType> l2 = KTypeVTypeOpenHashMap.from(
            newArray(key1, key2, key3),
            newvArray(value1, value2, value3));

        l0.putAll(l2);
        l1.putAll(l2);

        assertEquals(l0.hashCode(), l1.hashCode());
        assertEquals(l0, l1);
    }

    @Test
    public void testBug_HPPC37()
    {
        KTypeVTypeOpenHashMap<KType, VType> l1 = KTypeVTypeOpenHashMap.from(
            newArray(key1),
            newvArray(value1));

        KTypeVTypeOpenHashMap<KType, VType> l2 = KTypeVTypeOpenHashMap.from(
            newArray(key2),
            newvArray(value1));

        assertFalse(l1.equals(l2));
        assertFalse(l2.equals(l1));
    }    

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testNullKey()
    {
        map.put(null, vcast(10));
        assertEquals2(vcast(10), map.get(null));
        assertTrue(map.containsKey(null));
        assertEquals2(vcast(10), map.lget());
        assertEquals2(null, map.lkey());
        map.remove(null);
        assertEquals(0, map.size());
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    @SuppressWarnings("unchecked")
    public void testLkey()
    {
        map.put(key1, vcast(10));
        assertTrue(map.containsKey(key1));
        assertSame(key1, map.lkey());
        KType key1_ = (KType) new Integer(1);
        assertNotSame(key1, key1_);
        assertEquals(key1, key1_);
        assertTrue(map.containsKey(key1_));
        assertSame(key1, map.lkey());
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    @Test
    public void testNullValue()
    {
        map.put(key1, null);
        assertEquals(null, map.get(key1));
        assertTrue(map.containsKey(key1));
        map.remove(key1);
        assertFalse(map.containsKey(key1));
        assertEquals(0, map.size());
    }
    /*! #end !*/

    /*! #if ($TemplateOptions.AllGeneric) !*/
    /**
     * Run some random insertions/ deletions and compare the results
     * against <code>java.util.HashMap</code>.
     */
    @Test
    public void testAgainstHashMap()
    {
        final Random rnd = new Random(randomLong());
        final java.util.HashMap<KType, VType> other = 
            new java.util.HashMap<KType, VType>();

        for (int size = 1000; size < 20000; size += 4000)
        {
            other.clear();
            map.clear();

            for (int round = 0; round < size * 20; round++)
            {
                KType key = cast(rnd.nextInt(size));
                VType value = vcast(rnd.nextInt());

                if (rnd.nextBoolean())
                {
                    map.put(key, value);
                    other.put(key, value);

                    assertEquals(value, map.get(key));
                    assertTrue(map.containsKey(key));
                    assertEquals(value, map.lget());
                }
                else
                {
                    assertEquals(other.remove(key), map.remove(key));
                }

                assertEquals(other.size(), map.size());
            }
        }
    }
    /*! #end !*/
    
    /*
     * 
     */
    @Test
    public void testClone()
    {
        this.map.put(key1, value1);
        this.map.put(key2, value2);
        this.map.put(key3, value3);

        KTypeVTypeOpenHashMap<KType, VType> cloned = map.clone();
        cloned.remove(key1);

        assertSortedListEquals(map.keys().toArray(), key1, key2, key3);
        assertSortedListEquals(cloned.keys().toArray(), key2, key3);
    }

    /*
     * 
     */
    @Test
    public void testToString()
    {
        Assume.assumeTrue(
            (int[].class.isInstance(map.keys)     ||
             short[].class.isInstance(map.keys)   ||
             byte[].class.isInstance(map.keys)    ||
             long[].class.isInstance(map.keys)    ||
             Object[].class.isInstance(map.keys)) &&
            (int[].class.isInstance(map.values)   ||
             byte[].class.isInstance(map.values)  ||
             short[].class.isInstance(map.values) ||
             long[].class.isInstance(map.values)  ||
             Object[].class.isInstance(map.values)));

        this.map.put(key1, value1);
        this.map.put(key2, value2);

        String asString = map.toString();
        asString = asString.replaceAll("[^0-9]", "");
        char [] asCharArray = asString.toCharArray();
        Arrays.sort(asCharArray);
        assertEquals("1122", new String(asCharArray));
    }
    
    /* */
    @Test
    public void testMapValues()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);
        assertSortedListEquals(map.values().toArray(), value1, value2, value3);

        map.clear();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value2);
        assertSortedListEquals(map.values().toArray(), value1, value2, value2);        
    }

    /* */
    @Test
    public void testMapValuesIterator()
    {
        map.put(key1, value3);
        map.put(key2, value2);
        map.put(key3, value1);

        int counted = 0;
        for (KTypeCursor<VType> c : map.values())
        {
            assertEquals2(map.values[c.index], c.value);
            counted++;
        }
        assertEquals(counted, map.size());
    }

    /* */
    @Test
    public void testMapValuesContainer()
    {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value2);

        // contains()
        for (KTypeVTypeCursor<KType, VType> c : map)
            assertTrue(map.values().contains(c.value));
        assertFalse(map.values().contains(value3));
        
        assertEquals(map.isEmpty(), map.values().isEmpty());
        assertEquals(map.size(), map.values().size());

        final KTypeArrayList<VType> values = new KTypeArrayList<VType>();
        map.values().forEach(new KTypeProcedure<VType>()
            {
                public void apply(VType value)
                {
                    values.add(value);
                }
            });
        assertSortedListEquals(map.values().toArray(), value1, value2, value2);

        values.clear();
        map.values().forEach(new KTypePredicate<VType>()
            {
                public boolean apply(VType value)
                {
                    values.add(value);
                    return true;
                }
            });
        assertSortedListEquals(map.values().toArray(), value1, value2, value2);
    }
    
    /* */
    @Test
    public void testEqualsSameClass()
    {
        KTypeVTypeOpenHashMap<KType, VType> l1 = new KTypeVTypeOpenHashMap<>();
        l1.put(k1, value0);
        l1.put(k2, value1);
        l1.put(k3, value2);
  
        KTypeVTypeOpenHashMap<KType, VType> l2 = new KTypeVTypeOpenHashMap<>(l1);
        l2.putAll(l1);
  
        KTypeVTypeOpenHashMap<KType, VType> l3 = new KTypeVTypeOpenHashMap<>(l2);
        l3.putAll(l2);
        l3.put(k4, value0);

        Assertions.assertThat(l1).isEqualTo(l2);
        Assertions.assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
        Assertions.assertThat(l1).isNotEqualTo(l3);
    }

    /* */
    @Test
    public void testEqualsSubClass()
    {
        class Sub extends KTypeVTypeOpenHashMap<KType, VType> {
        };

        KTypeVTypeOpenHashMap<KType, VType> l1 = new KTypeVTypeOpenHashMap<>();
        l1.put(k1, value0);
        l1.put(k2, value1);
        l1.put(k3, value2);
  
        KTypeVTypeOpenHashMap<KType, VType> l2 = new Sub();
        l2.putAll(l1);
        l2.put(k4, value3);
  
        KTypeVTypeOpenHashMap<KType, VType> l3 = new Sub();
        l3.putAll(l2);

        Assertions.assertThat(l1).isNotEqualTo(l2);
        Assertions.assertThat(l2.hashCode()).isEqualTo(l3.hashCode());
        Assertions.assertThat(l2).isEqualTo(l3);
    }    
}
