import { Link } from "react-router";

import { Leaf, Salad } from "lucide-react";

import { FoodImage } from "./FoodImage";
import type { FoodItem } from "../../types/food.ts";
import { formatMacro } from "../../utils/format.ts";
import { CATEGORY_LABELS } from "../../constants/food.ts";
import { Card } from "../ui/Card.tsx";
import { Badge } from "../ui/badge.tsx";

interface FoodCardProps {
    food: FoodItem;
}

/** Nutrition stat row shown on every food card. */
function MacroRow({ food }: { food: FoodItem }) {
    const stats = [
        { label: "Calories", value: `${Math.round(food.calories)}` },
        { label: "Protein", value: `${formatMacro(food.protein)}g` },
        { label: "Carbs", value: `${formatMacro(food.carbohydrates)}g` },
        { label: "Fat", value: `${formatMacro(food.fat)}g` },
    ];

    return (
        <dl className="grid grid-cols-4 gap-2 border-t border-border pt-3">
            {stats.map((stat) => (
                <div key={stat.label} className="text-center">
                    <dt className="text-[11px] font-medium text-muted-foreground">
                        {stat.label}
                    </dt>

                    <dd className="mt-0.5 text-sm font-semibold text-foreground">
                        {stat.value}
                    </dd>
                </div>
            ))}
        </dl>
    );
}

/** Catalog card for a single food item. */
export function FoodCard({ food }: FoodCardProps) {
    // @ts-ignore
    return (
        <Link
            to={`/foods/${food.id}`}
            className="group block rounded-xl outline-none focus-visible:ring-2 focus-visible:ring-ring"
            aria-label={`View ${food.name}`}
        >
            <Card className="overflow-hidden transition-colors group-hover:border-primary/40">
                <div className="relative aspect-[4/3] overflow-hidden bg-muted">
                    <FoodImage
                        src={food.imageUrl ?? undefined}
                        alt={food.name}
                        category={food.category}
                        className="transition-transform duration-300 group-hover:scale-[1.03]"
                    />

                    <div className="absolute left-2.5 top-2.5 flex items-center gap-1.5">
                        <Badge
                            variant="secondary"
                            className="bg-background/85 backdrop-blur-sm"
                        >
                            {CATEGORY_LABELS[food.category]}
                        </Badge>

                        {food.vegetarian ? (
                            <Badge className="gap-1 bg-primary/90 text-primary-foreground">
                                <Leaf
                                    className="size-3"
                                    aria-hidden="true"
                                />
                                Veg
                            </Badge>
                        ) : null}
                    </div>
                </div>

                <div className="p-4">
                    <div className="flex items-start justify-between gap-3">
                        <h3 className="font-semibold leading-snug tracking-tight">
                            {food.name}
                        </h3>

                        <Salad
                            className="mt-0.5 size-4 shrink-0 text-muted-foreground/60"
                            aria-hidden="true"
                        />
                    </div>

                    <p className="mt-0.5 text-xs text-muted-foreground">
                        Per {food.servingSize.toLowerCase()}
                    </p>

                    <p className="mt-2 line-clamp-2 text-sm text-muted-foreground">
                        {food.description}
                    </p>

                    <div className="mt-4">
                        <MacroRow food={food} />
                    </div>
                </div>
            </Card>
        </Link>
    );
}