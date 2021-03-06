package cam72cam.immersiverailroading.track;

import cam72cam.immersiverailroading.Config.ConfigBalance;
import cam72cam.immersiverailroading.Config.ConfigDamage;
import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.util.BlockUtil;
import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.immersiverailroading.util.math.BlockPos;
import cam72cam.immersiverailroading.util.math.EnumFacing;
import cam72cam.immersiverailroading.util.math.MathHelper;
import cam72cam.immersiverailroading.util.math.Vec3d;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO @cam72cam use BlockPos and Vec3i

@SuppressWarnings("incomplete-switch")
public abstract class BuilderBase {
	protected ArrayList<TrackBase> tracks = new ArrayList<TrackBase>();
	
	public RailInfo info;

	public final BlockPos pos;
	private BlockPos parent_pos;

	public boolean overrideFlexible = false;

	public List<ItemStack> drops;

	public BuilderBase(RailInfo info, BlockPos pos) {
		this.info = info;
		this.pos = pos;
		parent_pos = pos;
	}

	public class VecYawPitch extends Vec3d {
		public final float yaw;
		public final float pitch;
		public final float length;
		public final List<String> groups;
		
		public VecYawPitch(double xIn, double yIn, double zIn, float yaw, String... groups) {
			this(xIn, yIn, zIn, yaw, 0, groups);
		}
		public VecYawPitch(double xIn, double yIn, double zIn, float yaw, float pitch, String... groups) {
			this(xIn, yIn, zIn, yaw, pitch, -1, groups);
		}
		public VecYawPitch(double xIn, double yIn, double zIn, float yaw, float pitch, float length, String... groups) {
			super(xIn, yIn, zIn);
			this.yaw = yaw;
			this.groups = Arrays.asList(groups);
			this.pitch = pitch;
			this.length = length;
		}
		public float getYaw() {
			return this.yaw;
		}
		public float getPitch() {
			return this.pitch;
		}
		public float getLength() {
			return this.length;
		}
		public List<String> getGroups() {
			return this.groups;
		}
	}
	
	public abstract List<VecYawPitch> getRenderData();

	public BlockPos convertRelativePositions(BlockPos rel) {
		return pos.add(rel);
	}
	
	public boolean canBuild() {
		for(TrackBase track : tracks) {
			if (!track.canPlaceTrack()) {
				return false;
			}
		}
		return true;
	}
	
	public void build() {
		/*
		Assume we have already tested.
		There are a few edge cases which break with overlapping split builders
		if (!canBuild()) {
			return ;
		}
		*/
		for(TrackBase track : tracks) {
			track.placeTrack().markDirty();
		}
	}
	
	public List<TrackBase> getTracksForRender() {
		return this.tracks;
	}

	
	public void setParentPos(BlockPos pos) {
		parent_pos = convertRelativePositions(pos);
	}
	public BlockPos getParentPos() {
		return parent_pos;
	}
	
	public int costTies() {
		return MathHelper.ceil(this.tracks.size()/3 * ConfigBalance.TieCostMultiplier);
	}
	
	public int costRails() {
		return MathHelper.ceil(this.tracks.size()*2/3 * ConfigBalance.RailCostMultiplier);
	}
	
	public int costBed() {
		//TODO more accurate
		return MathHelper.ceil(this.tracks.size() * 0.1 * ConfigBalance.BedCostMultiplier);
	}

	public int costFill() {
		int fillCount = 0;
		for (TrackBase track : tracks) {
			if (BlockUtil.canBeReplaced(info.world, track.getPos().down(), false)) {
				fillCount += 1;
			}
		}
		return MathHelper.ceil(this.info.settings.railBedFill.getItem() != null ? fillCount : 0);
	}

	public void setDrops(List<ItemStack> drops) {
		this.drops = drops;
	}

	public void clearArea() {
		for (TrackBase track : tracks) {
			for (int i = 0; i < 6 * info.settings.gauge.scale(); i++) {
				BlockPos main = track.getPos().up(i);
				if (!BlockUtil.isRail(info.world, main)) {
					info.world.func_147480_a(main.getX(), main.getY(), main.getZ(), false);
				}
				if (info.settings.gauge.isModel() && ConfigDamage.enableSideBlockClearing && info.settings.type != TrackItems.SLOPE && info.settings.type != TrackItems.TURNTABLE) {
					for (EnumFacing facing : EnumFacing.HORIZONTALS) {
						BlockPos pos = main.offset(facing);
						if (!BlockUtil.isRail(info.world, pos)) {
							info.world.func_147480_a(pos.getX(), pos.getY(), pos.getZ(), false);
						}
					}
				}
			}
			if (BlockUtil.canBeReplaced(info.world, track.getPos().down(), false)) {
				info.world.func_147480_a(track.getPos().down().getX(), track.getPos().down().getY(), track.getPos().down().getZ(), false);
			}
		}
	}
}
