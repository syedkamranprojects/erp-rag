import * as React from "react";
import { cn } from "@/lib/utils";

function initials(name: string): string {
  return name.trim().slice(0, 2).toUpperCase();
}

const Avatar = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement> & { name: string }>(
  ({ className, name, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        "flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary text-sm font-semibold text-primary-foreground",
        className,
      )}
      {...props}
    >
      {initials(name)}
    </div>
  ),
);
Avatar.displayName = "Avatar";

export { Avatar };
