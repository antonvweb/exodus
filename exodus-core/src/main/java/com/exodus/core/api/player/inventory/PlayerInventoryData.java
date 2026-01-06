package com.exodus.core.api.player.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerInventoryData {
    public final List<InventorySlot> slots;
    public static final int INVENTORY_SIZE = 16;

    public PlayerInventoryData(){
        this.slots = new ArrayList<>();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            this.slots.add(new InventorySlot());
        }
    }

    public boolean addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;  // Пустой стак - ничего не делаем
        }

        // ========== ШАГ 1: Ищем слоты с ТАКИМ ЖЕ предметом ==========

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            InventorySlot slot = slots.get(i);

            if (slot.isEmpty()) {
                continue;  // Пустой слот - пропускаем (ищем существующие стаки)
            }

            ItemStack existing = slot.getItemStack();

            // Проверяем: тот же предмет? (алмаз = алмаз)
            if (!ItemStack.isSameItemSameTags(existing, stack)) {
                continue;  // Другой предмет - пропускаем
            }

            // ===== НАШЛИ СЛОТ С ТАКИМ ЖЕ ПРЕДМЕТОМ! =====

            // 1. Сколько влезет?
            int maxStackSize = existing.getMaxStackSize();  // Обычно 64
            int currentCount = existing.getCount();         // Сколько уже есть
            int spaceLeft = maxStackSize - currentCount;    // Сколько еще влезет

            if (spaceLeft <= 0) {
                continue;  // Стак полный - пропускаем
            }

            // 2. Сколько добавим?
            int toAdd = Math.min(stack.getCount(), spaceLeft);

            // 3. Добавляем
            existing.grow(toAdd);     // Увеличить в слоте
            stack.shrink(toAdd);      // Уменьшить в руке

            // 4. Проверяем - всё добавлено?
            if (stack.isEmpty()) {
                return true;  // ✅ Готово!
            }

            // Если нет - продолжаем искать другие слоты
        }

        // ========== ШАГ 2: Не влезло в существующие → ищем ПУСТЫЕ слоты ==========

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            InventorySlot slot = slots.get(i);

            if (!slot.isEmpty()) {
                continue;  // Занятый - пропускаем
            }

            // ===== НАШЛИ ПУСТОЙ СЛОТ! =====

            // 1. Сколько можем положить?
            int maxStackSize = stack.getMaxStackSize();
            int toAdd = Math.min(stack.getCount(), maxStackSize);

            // 2. Создаем копию стака
            ItemStack newStack = stack.copy();
            newStack.setCount(toAdd);

            // 3. Кладем в слот
            slot.setItemStack(newStack);

            // 4. Уменьшаем оригинальный стак
            stack.shrink(toAdd);

            // 5. Проверяем - всё добавлено?
            if (stack.isEmpty()) {
                return true;  // ✅ Готово!
            }

            // Если нет - продолжаем искать другие пустые слоты
        }

        // ========== ШАГ 3: Не влезло ==========

        return false;  // ❌ Инвентарь полон!
    }

    public void removeItem(int index){
        slots.remove(index);
    }

    public void clear(){
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            this.slots.get(i).clear();
        }
    }

    public CompoundTag writeNbt(CompoundTag nbt) {
        ListTag itemsList = new ListTag();

        for (int i = 0; i < slots.size(); i++) {
            InventorySlot slot = slots.get(i);

            if (slot.isEmpty()) {
                continue;  // Пустые слоты пропускаем (экономим место)
            }

            // Создаем NBT для одного слота
            CompoundTag slotNbt = new CompoundTag();
            slotNbt.putInt("Slot", i);           // Индекс слота
            slot.writeNbt(slotNbt);               // Данные предмета

            itemsList.add(slotNbt);               // Добавляем в список
        }

        nbt.put("Items", itemsList);  // Сохраняем список
        return nbt;
    }

    public void readNbt(CompoundTag nbt) {
        // Сначала очищаем
        clear();

        if (!nbt.contains("Items")) {
            return;  // Нет сохраненных данных
        }

        ListTag itemsList = nbt.getList("Items", 10);  // 10 = тип CompoundTag

        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag slotNbt = itemsList.getCompound(i);
            int slotIndex = slotNbt.getInt("Slot");

            if (slotIndex >= 0 && slotIndex < INVENTORY_SIZE) {
                InventorySlot slot = slots.get(slotIndex);
                slot.readNbt(slotNbt);
            }
        }
    }
}
